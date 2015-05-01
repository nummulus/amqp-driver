package com.nummulus.amqp.driver.blackbox

import java.util.concurrent.atomic.AtomicLong

import scala.concurrent.Promise
import scala.util.Success

import com.nummulus.amqp.driver.api.provider.AmqpProviderRequest
import com.nummulus.amqp.driver.api.provider.AmqpResponseMessage

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef

private[blackbox] object BlackBoxHandlerActorScope {

  case class Initialize(actor: ActorRef)
  case object Finalize

  class BlackBoxHandlerActor(completed: Promise[Boolean]) extends Actor with ActorLogging {
    private var unanswered = Map.empty[Long, Promise[String]]
    private val tag = new AtomicLong(0)

    def receive = {
      case Initialize(actor) =>
        context.become(active(actor))
    }

    def active(actor: ActorRef): Receive = {
      case TellMessage(message) =>
        actor ! AmqpProviderRequest(message, tag.getAndIncrement())

      case AskMessage(message, promise) =>
        val deliveryTag = tag.getAndIncrement()
        unanswered += (deliveryTag -> promise)
        actor ! AmqpProviderRequest(message, deliveryTag)

      case AmqpResponseMessage(body, deliveryTag) => unanswered.get(deliveryTag) match {
        case Some(promise) =>
          unanswered -= deliveryTag
          promise.complete(Success(body))
        case None =>
          completed.failure(new IllegalStateException(s"Unknown deliveryTag $deliveryTag (with message $body)"))
          context.stop(self)
      }
      
      case Finalize =>
        completed.success(true)
    }
  }
}