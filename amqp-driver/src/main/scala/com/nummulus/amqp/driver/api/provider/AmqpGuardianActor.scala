package com.nummulus.amqp.driver.api.provider

import org.slf4j.LoggerFactory

import com.nummulus.amqp.driver.Channel
import com.nummulus.amqp.driver.IdGenerators
import com.nummulus.amqp.driver.MessageProperties
import com.nummulus.amqp.driver.akka.AkkaMessageConsumer
import com.nummulus.amqp.driver.akka.AmqpQueueMessageWithProperties
import com.nummulus.amqp.driver.configuration.QueueConfiguration

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Terminated
import akka.actor.TypedActor.PostStop

/**
 * Entry point for AMQP messages to enter the Akka world.
 * 
 * Monitors the actor which it sends messages to. If the monitored actor dies,
 * all unacknowledged messages will be requeued.
 */
private[driver] class AmqpGuardianActor(
    channel: Channel,
    configuration: QueueConfiguration,
    generateId: IdGenerators.IdGenerator = IdGenerators.random) extends Actor with PostStop {
  
  private val logger = LoggerFactory.getLogger(getClass)
  
  private val consumerTag = generateId()
  private val autoAcknowledge = configuration.autoAcknowledge
  
  private var unacknowledged = Set[Long]()
  private var unanswered = Map[Long, MessageProperties]()
  
  channel.queueDeclare(configuration.queue, configuration.durable, configuration.exclusive, configuration.autoDelete, null)
  logger.debug("Declared request queue: {}", configuration.queue)
  
  channel.basicQos(1)

  def receive = {
    case Bind(actor) =>
      context.become(bound(actor))
      context.watch(actor)
      
      val callback = new AkkaMessageConsumer(channel, self)
      channel.basicConsume(configuration.queue, autoAcknowledge, consumerTag, callback)
  }

  private def bound(actor: ActorRef): Receive = {
    /**
     * Handles an incoming message from the queue.
     */
    case AmqpQueueMessageWithProperties(body, properties, deliveryTag) => {
      if (!autoAcknowledge) unacknowledged += deliveryTag
      if (properties.replyTo != null && !properties.replyTo.isEmpty) unanswered += (deliveryTag -> properties)

      actor ! AmqpProviderRequest(body, deliveryTag)
    }

    /**
     * Handles a response message from another actor.
     */
    case AmqpProviderResponse(message, deliveryTag) => {
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
        if (unacknowledged contains deliveryTag) {
          unacknowledged -= deliveryTag
          channel.basicAck(deliveryTag, multiple = false)
        }
        else {
          logger.warn("Message with deliveryTag {} was already acknowledged", deliveryTag)
        }
      }
      else {
        logger.warn("Did not expect Acknowledge for autoAcknowledge channel (deliveryTag={})", deliveryTag)
      }
    }

    /**
     * Handles a terminate message from the watched actor by shutting down the
     * message consumer and stopping the guardian actor after its mailbox is
     * empty.
     */
    case _: Terminated => {
      channel.basicCancel(consumerTag)
      
      self ! PoisonPill
    }
    
    case unsupportedMessage => {
      logger.error("Received unsupported message {}", unsupportedMessage)
    }
  }
  
  /**
   * Requeues all unacknowledged messages and empties all unanswered messages.
   */
  override def postStop(): Unit = {
    unacknowledged foreach (channel.basicNack(_, multiple = false, requeue = true))
    unanswered = unanswered.empty
  }
}
