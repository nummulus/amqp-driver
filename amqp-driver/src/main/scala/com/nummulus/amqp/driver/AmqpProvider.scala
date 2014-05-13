package com.nummulus.amqp.driver

import _root_.akka.actor.ActorRef

/**
 * A provider listens to a well-known queue and provides a certain service.
 */
trait AmqpProvider {
  /**
   * Activates the provider. All messages that appear on the queue
   * are wrapped in an AmqpRequestMessage and sent to [[actor]].
   */
  def bind(actor: ActorRef): Unit

  /**
   * Unbinds the actor from the provider, and de-activates it.
   */
  def unbind(): Unit
}