package com.nummulus.amqp.driver.blackbox

import com.nummulus.amqp.driver.AmqpDriverFactory
import akka.actor.ActorSystem

private[blackbox] class BlackBoxAmqpDriverFactory(system: ActorSystem) extends AmqpDriverFactory {
  private val driver = new BlackBoxAmqpDriver(system)
  
  /**
   * Returns the singleton black box driver.
   */
  def apply(): BlackBoxAmqpDriver = driver
  
  /**
   * Returns the singleton black box driver, regardless of configFileName.
   */
  def apply(configFileName: String): BlackBoxAmqpDriver = driver
  
  private[blackbox] def done() = driver.done()
}