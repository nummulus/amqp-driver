package com.nummulus.amqp.driver.blackbox

import com.nummulus.amqp.driver.AmqpDriver
import com.nummulus.amqp.driver.AmqpProvider

import akka.actor.ActorRef
import akka.actor.ActorSystem

private[blackbox] class BlackBoxAmqpDriver(system: ActorSystem) extends AmqpDriver {
  private val providerConsumer = new BlackBoxAmqpProviderConsumer(system)
  
  /**
   * Not yet implemented.
   */
  def newAkkaConsumer(service: String, operation: String): ActorRef = ???

  /**
   * Returns the singleton black box provider, regardless of operation.
   */
  def newProvider(operation: String): AmqpProvider = providerConsumer

  private[blackbox] def done() = providerConsumer.done()
}