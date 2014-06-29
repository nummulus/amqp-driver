package com.nummulus.amqp.driver.blackbox
import scala.concurrent.Future
import scala.concurrent.Promise

import com.nummulus.amqp.driver.AmqpConsumer
import com.nummulus.amqp.driver.AmqpProvider
import com.nummulus.amqp.driver.AmqpProvider._

import BlackBoxHandlerActorScope._
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

class BlackBoxAmqpProviderConsumer(system: ActorSystem) extends AmqpProvider with AmqpConsumer {
  private var handler: Option[ActorRef] = None

  /**
   * Activates the black box provider. All messages that appear on the queue
   * are wrapped in an AmqpRequestMessage and sent to [[actor]].
   */
  def bind(actor: ActorRef): Unit = bind(_ => actor)
  
  /**
   * Activates the black box provider using an actor created by the actor factory.
   * All messages that appear on the queue are wrapped in an AmqpRequestMessage and sent to that actor.
   */
  def bind(createActor: ActorFactory): Unit = handler match {
    case None =>
      val handlerActor = system.actorOf(Props(classOf[BlackBoxHandlerActor]))
      handlerActor ! Initialize(createActor(handlerActor))
      handler = Some(handlerActor)
    case Some(_) =>
      throw new IllegalStateException("An actor was already bound to AmqpDriver")
  }

  /**
   * Unbinds the actor from the black box provider, and de-activates it.
   */
  def unbind(): Unit = handler match {
    case Some(h) =>
      system.stop(h)
      handler = None
    case None => noActorBound
  }
  
  /**
   * Sends a message asynchronously and returns a [[scala.concurrent.Future]]
   * holding the eventual response.
   */
  def ask(message: String): Future[String] = handler match {
    case Some(h) =>
      val promise = Promise[String]()
      h ! AskMessage(message, promise)
      promise.future
    case None => noActorBound
  }
  
  /**
   * Sends a message without waiting for a response, fire-and-forget semantics.
   */
  def tell(message: String): Unit = handler match {
    case Some(h) => h ! TellMessage(message)
    case None => noActorBound
  }
  
  private def noActorBound: Nothing =
    throw new IllegalStateException("No actor bound to AmqpDriver")
}
