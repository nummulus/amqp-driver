package com.nummulus.amqp.driver

import org.slf4j.LoggerFactory

import com.nummulus.amqp.driver.configuration.ConfigurationException
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.typesafe.config.Config
import com.typesafe.config.ConfigException

/**
 * Default driver implementation.
 * 
 * Every driver has a single connection with the broker. The connection is only
 * established if a consumer is created. Every consumer will get a separate
 * channel.
 */
private[driver] class DefaultDriver(connectionFactory: ConnectionFactory, config: Config) extends AmqpDriver {
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
   * @throws QueueConfiguration if the queue has missing keys in the configuration
   */
  override def newConsumer(service: String, operation: String): AmqpConsumer = {
    logger.info(s"Retrieving configuration for operation '$operation' on service '$service'")
    val queueConfiguration = getQueueConfiguration(service, operation)
    
    val channel = connection.createChannel()
    new DefaultConsumer(channel, queueConfiguration)
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
  
  /**
   * Returns the configuration of the request queue for the specified service
   * operation.
   * 
   * @throws QueueConfiguration if the queue has missing keys in the configuration
   */
  private def getQueueConfiguration(service: String, operation: String): QueueConfiguration = {
    try {
      val serviceConfig = rootConfig.getConfig(s"uses.$service")
      val operationConfig = serviceConfig.getConfig(operation)
      
      val serviceName = serviceConfig.getString("serviceName")
      val operationName = operationConfig.getString("queue")
      
      val queue = s"$serviceName.$operationName"
      
      QueueConfiguration(
          queue,
          operationConfig.getBoolean("durable"),
          operationConfig.getBoolean("exclusive"),
          operationConfig.getBoolean("autoDelete"),
          operationConfig.getBoolean("autoAcknowledge"))
    } catch {
      case e: ConfigException => throw new ConfigurationException(e.getMessage(), e)
    }
  }
}