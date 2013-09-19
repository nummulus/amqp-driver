package com.nummulus.amqp.driver

import org.slf4j.LoggerFactory
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.nummulus.amqp.driver.consumer.Delivery
import _root_.akka.actor.ActorRef
import _root_.akka.pattern.ask
import _root_.akka.pattern.AskTimeoutException
import _root_.akka.util.Timeout
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success
import scala.util.Failure
import ExecutionContext.Implicits.global

/**
 * Default provider implementation.
 */
private[driver] class DefaultProvider(channel: Channel, configuration: QueueConfiguration) extends AmqpProvider {
  implicit val timeout = Timeout(1.second)
  private val logger = LoggerFactory.getLogger(getClass)
  
  private val requestQueue = channel.queueDeclare(configuration.queue, configuration.durable, configuration.exclusive, configuration.autoDelete, null)
  logger.debug("Declared request queue: {}", requestQueue.getQueue)
  
  channel.basicQos(1)
  
  val callback = MessageConsumer.newBlocking(channel)
  channel.basicConsume(requestQueue.getQueue, configuration.autoAcknowledge, callback)
  
  var actor: Option[ActorRef] = None
  var serviceCallBack: Option[(String) => String] = None
  
  def bind(actor: ActorRef) {
    this.actor = Some(actor)
  }
  
  def bindCallBack(serviceCallBack: (String) => String) {
    this.serviceCallBack = Some(serviceCallBack)
  }
  
  def unbind() {
    this.actor = None
  }
  
  def handleNextDelivery(): Unit = {
    future {
      val delivery = callback.nextDelivery
      if (isQuestion(delivery))
        handleAsk(delivery)
      else
        handleTell(delivery)
    }
  }
  
  private def isQuestion(delivery: Delivery) = delivery.properties.correlationId != null
  
  private def handleTell(delivery: Delivery): Unit = {
    actor foreach { _ ! delivery.body.toString }
  }
  
  private def handleAsk(delivery: Delivery): Unit =  {
    serviceCallBack foreach { s =>
      val response = s(delivery.body.toString())
      val properties = MessageProperties(correlationId = delivery.properties.correlationId)
      channel.basicPublish("", configuration.queue, properties, response.getBytes)
    }
    
    actor foreach { a =>
      val response = a ? delivery.body.toString
      response onComplete {
        case Success(msg) =>
          val properties = MessageProperties(correlationId = delivery.properties.correlationId)
          channel.basicPublish("", configuration.queue, properties, msg.toString.getBytes)
        case Failure(e: AskTimeoutException) =>
          // TODO: do something smart when the response times out
          e.printStackTrace()
        case Failure(e) =>
          // TODO: do something smart when the response fails
          e.printStackTrace()
      }
    }
  }
}