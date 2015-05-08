package com.nummulus.amqp.driver.blackbox

import com.nummulus.amqp.driver.AmqpDriver

import akka.actor.ActorRef
import akka.actor.ActorSystem

private[blackbox] class BlackBoxAmqpDriver(system: ActorSystem) extends AmqpDriver {
  private val providerConsumer = new BlackBoxAmqpProviderConsumer(system)
  
  /**
   * Not yet implemented.
   */
  def newConsumer(service: String, operation: String): ActorRef = ???

  /**
   * Returns the singleton black box provider, regardless of operation.
   */
  def newProvider(operation: String): ActorRef = providerConsumer.provider()

  private[blackbox] def done() = providerConsumer.done()
}