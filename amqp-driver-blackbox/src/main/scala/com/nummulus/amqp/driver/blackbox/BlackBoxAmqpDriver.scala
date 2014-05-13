package com.nummulus.amqp.driver.blackbox

import com.nummulus.amqp.driver.AmqpConsumer
import com.nummulus.amqp.driver.AmqpDriver
import com.nummulus.amqp.driver.AmqpProvider

class BlackBoxAmqpDriver extends AmqpDriver {
  private val consumer = new BlackBoxAmqpConsumer
  private val provider = new BlackBoxAmqpProvider
  
  /**
   * Returns the singleton black box consumer, regardless of service and operation.
   */
  def newConsumer(service: String, operation: String): BlackBoxAmqpConsumer = consumer

  /**
   * Returns the singleton black box provider, regardless of operation.
   */
  def newProvider(operation: String): BlackBoxAmqpProvider = provider
}