package com.nummulus.amqp.driver

import _root_.akka.actor.ActorRef

/**
 * A provider listens to a well-known queue and provides a certain service.
 */
trait AmqpProvider {
  import AmqpProvider._

  /**
   * Activates the provider. All messages that appear on the queue
   * are wrapped in an AmqpRequestMessage and sent to [[actor]].
   */
  def bind(actor: ActorRef): Unit

  /**
   * Activates the provider with an actor created by the actor factory.
   * All messages that appear on the queue are wrapped in an AmqpRequestMessage
   * and sent to [[actor]].
   */
  def bind(createActor: ActorFactory): Unit

  /**
   * Unbinds the actor from the provider, and de-activates it.
   */
  def unbind(): Unit
}

object AmqpProvider {
  /**
   * Factory for an actor.
   * 
   * The function is given an actor that serves as the "sender" for the actor it returns.
   * Must return an actor that handles AmqpRequestMessages.
   */
  type ActorFactory = ActorRef => ActorRef
}