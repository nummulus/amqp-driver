package com.nummulus.amqp.driver

import java.util.UUID

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.slf4j.LoggerFactory

import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.nummulus.amqp.driver.consumer.MessageConsumer

/**
 * Default consumer implementation.
 */
private[driver] class DefaultConsumer(channel: Channel, configuration: QueueConfiguration, callback: => MessageConsumer) extends AmqpConsumer {
  private val logger = LoggerFactory.getLogger(getClass)
  
  private val responseQueue = channel.queueDeclare.getQueue
  logger.debug("Declared response queue: {}", responseQueue)
  
  private val requestQueue = channel.queueDeclare(configuration.queue, configuration.durable, configuration.exclusive, configuration.autoDelete, null)
  logger.debug("Declared request queue: {}", requestQueue.getQueue)
  
  channel.basicConsume(responseQueue, configuration.autoAcknowledge, callback)
  
  /**
   * Sends a message asynchronously and returns a [[scala.concurrent.Future]]
   * holding the eventual response.
   * 
   * Will listen indefinitely on the response queue until a response arrives.
   */
  override def ask(message: String): Future[String] = {
    val correlationId = UUID.randomUUID.toString
    
    val properties = MessageProperties(
        correlationId = correlationId,
        replyTo = responseQueue)
    
    logger.debug("Sending message to queue: {}", message)
    channel.basicPublish("", configuration.queue, properties, message.getBytes)
    
    future {
      ???
    }
  }
}