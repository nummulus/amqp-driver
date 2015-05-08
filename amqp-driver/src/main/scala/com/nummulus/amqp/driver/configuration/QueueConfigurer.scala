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
   * @param operation name of the operation
   * @param queueRootConfig root of the queues configuration
   * @throws ConfigurationException if the queue has missing keys in the configuration file
   */
  private def getQueueConfiguration(rootConfig: Config, operation: String, pathToRoot: => String): QueueConfiguration = {
    try {
      val queueRootConfig = rootConfig.getConfig(pathToRoot) //defines
      val operationConfig = queueRootConfig.getConfig(operation) //operation_one
      
      val serviceName = queueRootConfig.getString("serviceName") //service.test
      val operationName = operationConfig.getString("queue") //Test
      
      val queue = s"$serviceName.$operationName" //service.test.Test
      
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