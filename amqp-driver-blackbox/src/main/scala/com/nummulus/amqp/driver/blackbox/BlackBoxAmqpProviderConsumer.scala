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
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.PoisonPill

class BlackBoxAmqpProviderConsumer(system: ActorSystem) extends AmqpProvider with AmqpConsumer {
  private var handler: Option[ActorRef] = None

  /**
   * Activates the black box provider. All messages that appear on the queue
   * are wrapped in an AmqpRequestMessage and sent to [[actor]].
   */
  def bind(actor: ActorRef): Unit = {
    val props = Props(classOf[BlackBoxHandlerActor], actor)
    handler = Some(system.actorOf(props))
  }

  /**
   * Unbinds the actor from the black box provider, and de-activates it.
   */
  def unbind(): Unit = handler match {
    case None => noActorBound
    case Some(h) =>
      system.stop(h)
      handler = None
  }
  
  /**
   * Sends a message asynchronously and returns a [[scala.concurrent.Future]]
   * holding the eventual response.
   */
  def ask(message: String): Future[String] = handler match {
    case None => noActorBound
    case Some(h) =>
      val promise = Promise[String]()
      h ! AskMessage(message, promise)
      promise.future
  }
  
  /**
   * Sends a message without waiting for a response, fire-and-forget semantics.
   */
  def tell(message: String): Unit = handler match {
    case None => noActorBound
    case Some(h) => h ! TellMessage(message)
  }
  
  private def noActorBound: Nothing =
    throw new IllegalStateException("No actor bound to AmqpDriver")
}
