package com.nummulus.amqp.driver

import _root_.akka.actor.ActorRef

/**
 * A driver is a factory for service providers and consumers.
 */
trait AmqpDriver {
  /**
   * Returns an actor which can communicate with the services' operation.
   * 
   * @param service name of the service owning the operation to consume
   * @param operation name of the operation to consume
   * @return new consumer
   */
  def newConsumer(service: String, operation: String): ActorRef
  
  /**
   * Returns an actor which acts as a liaison for a services' operation.
   * 
   * @param operation name of the operation to provide
   * @return new provider
   */
  def newProvider(operation: String): ActorRef
}
