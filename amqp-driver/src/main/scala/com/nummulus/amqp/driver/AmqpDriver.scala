package com.nummulus.amqp.driver

import com.rabbitmq.client.{ConnectionFactory => RabbitConnectionFactory}
import com.typesafe.config.ConfigFactory


/**
 * A driver is a factory for service providers and consumers.
 */
trait AmqpDriver {
  /**
   * Returns a new consumer for a services' operation.
   * 
   * @param service name of the service owning the operation to consume
   * @param operation name of the operation to consume
   * @return new consumer
   */
  def newConsumer(service: String, operation: String): AmqpConsumer
  
  /**
   * Returns a new provider for a services' operation.
   * 
   * @param operation name of the operation to provide
   * @return new provider
   */
  def newProvider(operation: String): AmqpProvider
}

/**
 * Factory for the AMQP driver.
 */
object AmqpDriver {
  private val connectionFactory = new ConnectionFactory(new RabbitConnectionFactory)
  
  /**
   * Returns a new driver with the configuration loaded from "application.conf".
   */
  def apply: AmqpDriver = new DefaultDriver(connectionFactory, ConfigFactory.load)
  
  /**
   * Returns a new driver with the configuration loaded from the specified configuration file.
   */
  def apply(configFileName: String): AmqpDriver = new DefaultDriver(connectionFactory, ConfigFactory.load(configFileName)) 
}
