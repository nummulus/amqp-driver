package com.nummulus.amqp.driver

import org.slf4j.LoggerFactory
import com.nummulus.amqp.driver.configuration.ConfigurationException
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.nummulus.amqp.driver.consumer.MessageConsumer
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
   * @throws QueueConfiguration if the queue has missing keys in the configuration file
   */
  override def newConsumer(service: String, operation: String): AmqpConsumer = {
    logger.info(s"Retrieving configuration for operation '$operation' on service '$service'")
    val queueConfiguration = getConsumerQueueConfiguration(service, operation)
    
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
    val queueConfiguration = getProvideQueuerConfiguration(operation)
    
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
  
  /**
   * Returns the queue configuration of a consumer's service operation.
   * 
   * @throws QueueConfiguration if the queue has missing keys in the configuration file
   */
  private def getConsumerQueueConfiguration(service: String, operation: String): QueueConfiguration =
    getQueueConfiguration(operation, s"uses.$service")
  
  /**
   * Returns the queue configuration of a provider's service operation.
   * 
   * @throws QueueConfiguration if the queue has missing keys in the configuration file
   */
  private def getProvideQueuerConfiguration(operation: String): QueueConfiguration =
    getQueueConfiguration(operation, "defines")
  
  /**
   * Returns the configuration of the request queue for the specified service
   * operation.
   * 
   * @param operation name of the operation
   * @param queueRootConfig root of the queues configuration
   * @throws QueueConfiguration if the queue has missing keys in the configuration file
   */
  private def getQueueConfiguration(operation: String, pathToRoot: => String): QueueConfiguration = {
    try {
      val queueRootConfig = rootConfig.getConfig(pathToRoot)
      val operationConfig = queueRootConfig.getConfig(operation)
      
      val serviceName = queueRootConfig.getString("serviceName")
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