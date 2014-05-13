package com.nummulus.amqp.driver.blackbox

import scala.concurrent.Future
import com.nummulus.amqp.driver.AmqpConsumer
import com.nummulus.amqp.driver.AmqpProvider
import akka.actor.ActorRef
import scala.actors.threadpool.AtomicInteger
import com.nummulus.amqp.driver.akka.AmqpRequestMessage

class BlackBoxAmqpProviderConsumer extends AmqpProvider with AmqpConsumer {
  private var boundActor: Option[ActorRef] = None
  private val tag = new AtomicInteger(0)

  /**
   * Activates the black box provider. All messages that appear on the queue
   * are wrapped in an AmqpRequestMessage and sent to [[actor]].
   */
  def bind(actor: ActorRef): Unit =
    boundActor = Some(actor)

  /**
   * Unbinds the actor from the black box provider, and de-activates it.
   */
  def unbind(): Unit =
    boundActor = None

  /**
   * Sends a message asynchronously and returns a [[scala.concurrent.Future]]
   * holding the eventual response.
   */
  def ask(message: String): Future[String] = boundActor match {
    case Some(actor) => null // do something
    case None => noActorBound
  }
  
  /**
   * Sends a message without waiting for a response, fire-and-forget semantics.
   */
  def tell(message: String): Unit = boundActor match {
    case Some(actor) => actor ! AmqpRequestMessage(message, tag.getAndIncrement())
    case None => noActorBound
  }
  
  private def noActorBound: Nothing =
    throw new IllegalStateException("No actor bound to AmqpDriver")
}