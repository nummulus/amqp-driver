package com.nummulus.amqp.driver

import scala.annotation.tailrec
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.slf4j.LoggerFactory
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.nummulus.amqp.driver.consumer.BlockingMessageConsumer
import com.nummulus.amqp.driver.consumer.CorrelationIdGenerator
import com.nummulus.amqp.driver.consumer.RandomCorrelationIdGenerator
import java.nio.charset.StandardCharsets

/**
 * Default consumer implementation.
 */
private[driver] class DefaultConsumer(channel: Channel, configuration: QueueConfiguration, callback: BlockingMessageConsumer, correlationIdGenerator: CorrelationIdGenerator = new RandomCorrelationIdGenerator) extends AmqpConsumer {
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
    val correlationId = correlationIdGenerator.generate
    
    val properties = MessageProperties(
        correlationId = correlationId,
        replyTo = responseQueue)
    
    logger.debug("Sending message to queue: {}", message)
    logger.debug("Properties = {}", properties)
    channel.basicPublish("", configuration.queue, properties, message.getBytes)
    
    future {
      waitForDelivery(correlationId)
    }
  }
  
  /**
   * Sends a message without waiting for a response, fire-and-forget semantics.
   */
  override def tell(message: String) {
    logger.debug("Sending message to fire-and-forget queue: {}", message)
    channel.basicPublish("", configuration.queue, null, message.getBytes)
  }
  
  /**
   * Returns the response message belonging to a request with the specified
   * correlationId.
   * 
   * Requests with the wrong correlationId are ignored.
   */
  @tailrec
  private def waitForDelivery(correlationId: String): String = {
    logger.debug("Waiting for a response with correlation id {}", correlationId)
    val delivery = callback.nextDelivery
    if (delivery.properties.correlationId == correlationId) {
      logger.debug("Response received")
      channel.basicAck(delivery.deliveryTag, false)
      new String(delivery.body, StandardCharsets.UTF_8)
    }
    else {
      logger.debug("Received response with wrong correlationId")
      waitForDelivery(correlationId)
    }
  }
}