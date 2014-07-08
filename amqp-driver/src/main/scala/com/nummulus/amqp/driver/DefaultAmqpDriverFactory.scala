package com.nummulus.amqp.driver

import com.rabbitmq.client.{ConnectionFactory => RabbitConnectionFactory}
import com.typesafe.config.ConfigFactory

/**
 * Factory for the Default AMQP driver.
 */
object DefaultAmqpDriverFactory extends AmqpDriverFactory {
  private val connectionFactory = new ConnectionFactory(new RabbitConnectionFactory)
  
  /**
   * Returns a new driver with the configuration loaded from "application.conf".
   */
  def apply(): AmqpDriver = new DefaultDriver(connectionFactory, ConfigFactory.load)
  
  /**
   * Returns a new driver with the configuration loaded from the specified configuration file.
   */
  def apply(configFileName: String): AmqpDriver = new DefaultDriver(connectionFactory, ConfigFactory.load(configFileName)) 
}
