package com.nummulus.amqp.driver

import org.slf4j.LoggerFactory

import com.nummulus.amqp.driver.api.provider.AmqpGuardianActor
import com.nummulus.amqp.driver.configuration.TimeOutConfigurer
import com.nummulus.amqp.driver.configuration.QueueConfigurer
import com.typesafe.config.Config

import _root_.akka.actor.ActorRef
import _root_.akka.actor.ActorSystem
import _root_.akka.actor.Props

/**
 * Default driver implementation.
 * 
 * Every driver has a single connection with the broker. The connection is only
 * established if a consumer is created. Every consumer will get a separate
 * channel.
 */
private[driver] class DefaultDriver(connectionFactory: ConnectionFactory, config: Config) extends AmqpDriver
    with QueueConfigurer
    with TimeOutConfigurer {

  private val logger = LoggerFactory.getLogger(getClass)
  private val rootConfig = config.getConfig("amqp")
  
  private lazy val actorSystem = ActorSystem("AmqpDriver")
  private lazy val connection = createConnection()
  
  /**
   * Returns an actor which can communicate with the services' operation.
   * 
   * @param service name of the service owning the operation to consume
   * @param operation name of the operation to consume
   * @return new consumer
   */
  override def newConsumer(service: String, operation: String): ActorRef = {
    logger.info(s"Retrieving configuration for operation '$operation' on service '$service'")
    val queueConfiguration = getConsumerQueueConfiguration(rootConfig, service, operation)
    
    val channel = connection.createChannel()
    val timeOut = getConsumerTimeOut(rootConfig, service, operation)
    actorSystem.actorOf(Props(classOf[DefaultConsumer], channel, queueConfiguration, timeOut, IdGenerators.random))
  }
  
  /**
   * Returns an actor which acts as a liaison for a services' operation.
   * 
   * @param operation name of the operation to provide
   * @return new provider
   * @throws QueueConfiguration if the queue has missing keys in the configuration file
   */
  override def newProvider(operation: String): ActorRef = {
    logger.info(s"Retrieving configuration for operation '$operation'")
    val queueConfiguration = getProvideQueuerConfiguration(rootConfig, operation)
    
    val channel = connection.createChannel()
    actorSystem.actorOf(Props(classOf[AmqpGuardianActor], channel, queueConfiguration, IdGenerators.random), queueConfiguration.queue + "Guardian")
  }
  
  /**
   * Returns a newly created connection to the broker.
   */
  private def createConnection(): Connection = {
    val host = rootConfig.getString("host")
    
    logger.info("Connecting to AMQP broker at {}", host)
    connectionFactory.setHost(host)
    connectionFactory.newConnection()
  }
  

}