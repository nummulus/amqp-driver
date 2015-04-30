package com.nummulus.amqp.driver

import _root_.akka.actor.ActorRef

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
   * Returns an actor which can communicate with the services' operation.
   * 
   * @param service name of the service owning the operation to consume
   * @param operation name of the operation to consume
   * @return new consumer
   */
  def newAkkaConsumer(service: String, operation: String): ActorRef
  
  /**
   * Returns a new provider for a services' operation.
   * 
   * @param operation name of the operation to provide
   * @return new provider
   */
  def newProvider(operation: String): AmqpProvider
}
