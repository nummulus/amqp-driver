package com.nummulus.amqp.driver

import com.nummulus.amqp.driver.consumer.BlockingMessageConsumer

import com.rabbitmq.client.{Consumer => RabbitConsumer}

/**
 * A message consumer receives messages from a queue and processes them.
 */
trait MessageConsumer {
  /**
   * Returns the underlying RabbitMQ consumer.
   */
  private[driver] def get: RabbitConsumer
}

/**
 * Factory for creating message consumers.
 */
object MessageConsumer {
  /**
   * Returns a new blocking message consumer.
   */
  def newBlocking(channel: Channel): BlockingMessageConsumer = new BlockingMessageConsumer(channel)
}