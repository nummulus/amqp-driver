package com.nummulus.amqp.driver.akka

import com.nummulus.amqp.driver.Channel
import com.nummulus.amqp.driver.MessageProperties
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Terminated
import org.slf4j.LoggerFactory

/**
 * Entry point for AMQP messages to enter the Akka world.
 * 
 * Monitors the actor which it sends messages to. If the monitored actor dies,
 * all unacknowledged messages will be requeued.
 */
private[driver] class AmqpGuardianActor(actor: ActorRef, channel: Channel, configuration: QueueConfiguration) extends Actor {
  private val logger = LoggerFactory.getLogger(getClass)
  private var unacknowledged = Set[Long]()
  private var unanswered = Map[Long, MessageProperties]()
  private val autoAcknowledge = configuration.autoAcknowledge
  
  context.watch(actor)
  
  override def receive = {
    /**
     * Handles an incoming message from the queue.
     */
    case AmqpRequestMessageWithProperties(body, properties, deliveryTag) => {
      if (!autoAcknowledge) unacknowledged += deliveryTag
      if (properties.replyTo != null && !properties.replyTo.isEmpty) unanswered += (deliveryTag -> properties)
      
      actor ! AmqpRequestMessage(body, deliveryTag)
    }
    
    /**
     * Handles a response message from another actor.
     */
    case AmqpResponseMessage(message, deliveryTag) => {
      if (!autoAcknowledge) self ! Acknowledge(deliveryTag)
      
      if (unanswered contains deliveryTag) {
        val requestProperties = unanswered(deliveryTag)
        unanswered -= deliveryTag
        
        val responseProperties = MessageProperties(correlationId = requestProperties.correlationId)
        
        channel.basicPublish("", requestProperties.replyTo, responseProperties, message.getBytes)
      }
      else {
        logger.warn("Did not expect Response for deliveryTag {}: message was either fire-and-forget or already responded to", deliveryTag)
      }
    }
    
    /**
     * Handles an acknowledge message from another actor.
     */
    case Acknowledge(deliveryTag) => {
      if (!autoAcknowledge) {
        unacknowledged -= deliveryTag
        channel.basicAck(deliveryTag, false)
      }
      else {
        logger.warn("Did not expect Acknowledge for autoAcknowledge channel (deliveryTag={})", deliveryTag)
      }
    }
    
    /**
     * Handles a terminate message from the watched actor by requeueing all unacknowledged messages.
     */
    case _: Terminated => {
      unacknowledged foreach (channel.basicNack(_, false, true))
      unanswered = unanswered.empty
    }
    
    case x => {
      logger.error("Got unknown message {}", x)
    }
  }
}