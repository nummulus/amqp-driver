package com.nummulus.amqp.driver.blackbox

import com.nummulus.amqp.driver.AmqpConsumer
import com.nummulus.amqp.driver.AmqpDriver
import com.nummulus.amqp.driver.AmqpProvider
import akka.actor.ActorSystem

class BlackBoxAmqpDriver(system: ActorSystem) extends AmqpDriver {
  private val providerConsumer = new BlackBoxAmqpProviderConsumer
  
  /**
   * Returns the singleton black box consumer, regardless of service and operation.
   */
  def newConsumer(service: String, operation: String): AmqpConsumer = providerConsumer

  /**
   * Returns the singleton black box provider, regardless of operation.
   */
  def newProvider(operation: String): AmqpProvider = providerConsumer
}