package com.nummulus.amqp.driver.blackbox

import com.nummulus.amqp.driver.AmqpProvider
import akka.actor.ActorRef

class BlackBoxAmqpProvider extends AmqpProvider {
  /**
   * Activates the black box provider. All messages that appear on the queue
   * are wrapped in an AmqpRequestMessage and sent to [[actor]].
   */
  def bind(actor: ActorRef): Unit = ???

  /**
   * Unbinds the actor from the black box provider, and de-activates it.
   */
  def unbind(): Unit = ???
}