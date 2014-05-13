package com.nummulus.amqp.driver.blackbox

import com.nummulus.amqp.driver.AmqpDriverFactory

object BlackBoxAmqpDriverFactory extends AmqpDriverFactory {
  private val driver = new BlackBoxAmqpDriver
  
  /**
   * Returns the singleton black box driver.
   */
  def apply: BlackBoxAmqpDriver = driver
  
  /**
   * Returns the singleton black box driver, regardless of configFileName.
   */
  def apply(configFileName: String): BlackBoxAmqpDriver = driver
}