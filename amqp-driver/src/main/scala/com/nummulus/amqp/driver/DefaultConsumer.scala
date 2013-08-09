package com.nummulus.amqp.driver

import org.slf4j.LoggerFactory

import com.nummulus.amqp.driver.configuration.QueueConfiguration

/**
 * Default consumer implementation.
 */
private[driver] class DefaultConsumer(channel: Channel, configuration: QueueConfiguration) extends AmqpConsumer {
  private val logger = LoggerFactory.getLogger(getClass)
  
  private val responseQueue = channel.queueDeclare.getQueue
  logger.debug("Declared response queue: {}", responseQueue)
  
  private val requestQueue = channel.queueDeclare(configuration.queue, configuration.durable, configuration.exclusive, configuration.autoDelete, null)
  logger.debug("Declared request queue: {}", requestQueue.getQueue)
}