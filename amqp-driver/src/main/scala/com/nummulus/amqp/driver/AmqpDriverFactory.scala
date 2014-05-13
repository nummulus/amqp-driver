package com.nummulus.amqp.driver

/**
 * Factory for the AMQP driver.
 */
trait AmqpDriverFactory {
  /**
   * Returns a new driver with the configuration loaded from "application.conf".
   */
  def apply: AmqpDriver
  
  /**
   * Returns a new driver with the configuration loaded from the specified configuration file.
   */
  def apply(configFileName: String): AmqpDriver
}
