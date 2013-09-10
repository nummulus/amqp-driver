package com.nummulus.amqp.driver

import org.slf4j.LoggerFactory
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.nummulus.amqp.driver.consumer.Delivery
import _root_.akka.actor.ActorRef
import _root_.akka.pattern.ask
import _root_.akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success

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
  
  def bind(actor: ActorRef) {
    this.actor = Some(actor)
  }
  
  def unbind() {
    this.actor = None
  }
  
  def handleNextDelivery() {
    val delivery = callback.nextDelivery

    if (isQuestion(delivery))
      handleAsk(delivery)
    else
      handleTell(delivery)
  }
  
  private def isQuestion(delivery: Delivery) = delivery.properties.correlationId != null
  
  private def handleTell(delivery: Delivery): Unit = {
    actor foreach { _ ! delivery.body.toString }
  }
  
  private def handleAsk(delivery: Delivery): Unit =  {
    actor foreach { a =>
      val response = a ? delivery.body.toString
      // TODO: make this asynchronous
      val msg = Await.result(response, timeout.duration)
      val properties = MessageProperties(correlationId = delivery.properties.correlationId)
      channel.basicPublish("", configuration.queue, properties, msg.toString.getBytes)
    }
  }
}