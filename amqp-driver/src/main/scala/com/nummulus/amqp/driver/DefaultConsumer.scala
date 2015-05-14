package com.nummulus.amqp.driver

import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration

import org.slf4j.LoggerFactory

import com.nummulus.amqp.driver.IdGenerators._
import com.nummulus.amqp.driver.akka.AkkaMessageConsumer
import com.nummulus.amqp.driver.akka.AmqpQueueMessageWithProperties
import com.nummulus.amqp.driver.api.consumer.AmqpConsumerRequest
import com.nummulus.amqp.driver.api.consumer.AmqpConsumerRequestTimedOut
import com.nummulus.amqp.driver.api.consumer.AmqpConsumerResponse
import com.nummulus.amqp.driver.api.consumer.RequestTimedOut
import com.nummulus.amqp.driver.configuration.QueueConfiguration

import _root_.akka.actor.Actor
import _root_.akka.actor.ActorRef
import _root_.akka.actor.Scheduler

class DefaultConsumer(
    channel: Channel,
    configuration: QueueConfiguration,
    timeOut: Duration,
    generateId: IdGenerator = IdGenerators.random) extends Actor {
  
  private val logger = LoggerFactory.getLogger(getClass)
  
  private val responseQueue = channel.queueDeclare().getQueue
  logger.debug("Declared response queue: {}", responseQueue)
  
  private val requestQueue = channel.queueDeclare(configuration.queue, configuration.durable, configuration.exclusive, configuration.autoDelete, null)
  logger.debug("Declared request queue: {}", requestQueue.getQueue)
  
  val callback = new AkkaMessageConsumer(channel, self)
  channel.basicConsume(responseQueue, configuration.autoAcknowledge, generateId(), callback)
  
  private val pending = scala.collection.mutable.Map[String, Option[ActorRef]]()

  def receive = {
    /**
     * Handles a request from another actor.
     */
    case AmqpConsumerRequest(body, sender) => {
      val properties = MessageProperties(
        correlationId = generateId(),
        replyTo = responseQueue)
      
      pending += (properties.correlationId -> sender) 
      
      logger.debug("Sending message to queue: {}", body)
      logger.debug("Properties = {}", properties)
      channel.basicPublish("", configuration.queue, properties, body.getBytes)

      if (sender.isDefined && timeOut.isFinite()) {
        import context.dispatcher

        logger.debug("Schedule time-out message to be sent in {}ms", timeOut.length)
        scheduler.scheduleOnce(
          timeOut.asInstanceOf[FiniteDuration],
          self,
          RequestTimedOut(properties.correlationId))
      }
    }
    
    /**
     * Handles an incoming response from the queue.
     */
    case AmqpQueueMessageWithProperties(body, properties, deliveryTag) => {
      logger.info("Received a response from the service")
      val correlationId = properties.correlationId
      
      pending filterKeys (_ == correlationId) foreach {
        case (_, Some(sender)) => sender ! AmqpConsumerResponse(body)
        case (_, None) => // Do nothing
      }
      
      if (pending contains correlationId) {
        pending -= correlationId
      }
      else {
        logger.warn("Did not expect a response with correlationId {}", correlationId)
      }
    }

    /**
     * Handles a request time-out.
     */
    case RequestTimedOut(correlationId) => {
      pending filterKeys (_ == correlationId) foreach {
        case (_, Some(sender)) => sender ! AmqpConsumerRequestTimedOut
        case (_, None) => // Response was received before the time-out
      }

      if (pending contains correlationId) {
        pending -= correlationId
      }
    }
  }

  /**
   * Scheduler for time-out messages.
   */
  private[driver] def scheduler: Scheduler = context.system.scheduler
}
