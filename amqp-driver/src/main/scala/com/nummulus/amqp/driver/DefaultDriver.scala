package com.nummulus.amqp.driver

import org.slf4j.LoggerFactory
import com.nummulus.amqp.driver.configuration.ConfigurationException
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.nummulus.amqp.driver.configuration.QueueConfigurer

/**
 * Default driver implementation.
 * 
 * Every driver has a single connection with the broker. The connection is only
 * established if a consumer is created. Every consumer will get a separate
 * channel.
 */
private[driver] class DefaultDriver(connectionFactory: ConnectionFactory, config: Config) extends AmqpDriver with QueueConfigurer {
  private val logger = LoggerFactory.getLogger(getClass)
  private val rootConfig = config.getConfig("amqp")
  
  private lazy val connection = createConnection()
  
  /**
   * Creates a new consumer for the specified service operation.
   * 
   * If no connection to the broker is available, one will be established.
   * 
   * @param service owner of the operation
   * @param operation operation name of the operation to invoke
   * @return new consumer
   * @throws QueueConfiguration if the queue has missing keys in the configuration file
   */
  override def newConsumer(service: String, operation: String): AmqpConsumer = {
    logger.info(s"Retrieving configuration for operation '$operation' on service '$service'")
    val queueConfiguration = getConsumerQueueConfiguration(rootConfig, service, operation)
    
    val channel = connection.createChannel()
    new DefaultConsumer(channel, queueConfiguration, MessageConsumer.newBlocking(channel))
  }
  
  /**
   * Returns a new provider for a services' operation.
   * 
   * @param operation name of the operation to provide
   * @return new provider
   * @throws QueueConfiguration if the queue has missing keys in the configuration file
   */
  override def newProvider(operation: String): AmqpProvider = {
    logger.info(s"Retrieving configuration for operation '$operation'")
    val queueConfiguration = getProvideQueuerConfiguration(rootConfig, operation)
    
    val channel = connection.createChannel()
    new DefaultProvider(channel, queueConfiguration)
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