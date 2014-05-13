package com.nummulus.amqp.driver.blackbox

import java.util.concurrent.atomic.AtomicLong

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.Success

import com.nummulus.amqp.driver.AmqpConsumer
import com.nummulus.amqp.driver.AmqpProvider
import com.nummulus.amqp.driver.akka.AmqpRequestMessage
import com.nummulus.amqp.driver.akka.AmqpResponseMessage

import akka.actor.Actor
import akka.actor.ActorRef

class BlackBoxAmqpProviderConsumer extends AmqpProvider with AmqpConsumer with Actor {
  private var boundActor: Option[ActorRef] = None
  private var unanswered = Map.empty[Long, Promise[String]]
  private val tag = new AtomicLong(0)

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
    case Some(actor) => handleAsk(message, actor)
    case None => noActorBound
  }
  
  /**
   * Sends a message without waiting for a response, fire-and-forget semantics.
   */
  def tell(message: String): Unit = boundActor match {
    case Some(actor) => handleTell(message, actor)
    case None => noActorBound
  }
  
  override def receive = {
    case AmqpResponseMessage(body, deliveryTag) => unanswered.get(deliveryTag) match {
      case Some(promise) =>
        unanswered -= deliveryTag
        promise.complete(Success(body))
      case None =>
        throw new IllegalStateException(s"Unknown deliveryTag $deliveryTag (with message $body)")
    }
  }
  
  private def handleAsk(message: String, actor: ActorRef): Future[String] = {
    val deliveryTag = tag.getAndIncrement()
    val promise = Promise[String]()
    unanswered += (deliveryTag -> promise)
    actor ! AmqpRequestMessage(message, deliveryTag)
    promise.future
  }

  private def handleTell(message: String, actor: ActorRef): Unit =
    actor ! AmqpRequestMessage(message, tag.getAndIncrement())
  
  private def noActorBound: Nothing =
    throw new IllegalStateException("No actor bound to AmqpDriver")
}