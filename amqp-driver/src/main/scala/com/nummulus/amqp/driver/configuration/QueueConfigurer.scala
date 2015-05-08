package com.nummulus.amqp.driver.configuration

import com.typesafe.config.Config
import com.typesafe.config.ConfigException

/**
 * Provides convenience for configuring queues for providers and consumerss
 */
private[driver] trait QueueConfigurer {
  /**
   * Returns the queue configuration of a consumer's service operation.
   * 
   * @throws ConfigurationException if the queue has missing keys in the configuration file
   */
  def getConsumerQueueConfiguration(rootConfig: Config, service: String, operation: String): QueueConfiguration =
    getQueueConfiguration(rootConfig, operation, s"uses.$service")
  
  /**
   * Returns the queue configuration of a provider's service operation.
   * 
   * @throws ConfigurationException if the queue has missing keys in the configuration file
   */
  def getProvideQueuerConfiguration(rootConfig: Config, operation: String): QueueConfiguration =
    getQueueConfiguration(rootConfig, operation, "defines")
  
  /**
   * Returns the configuration of the request queue for the specified service
   * operation.
   *
   * @param rootConfig root of the driver's configuration
   * @param operation name of the operation
   * @param pathToRoot path to the root of the queue's configuration
   * @throws ConfigurationException if the queue has missing keys in the configuration file
   */
  private def getQueueConfiguration(rootConfig: Config, operation: String, pathToRoot: => String): QueueConfiguration = {
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