package com.nummulus.amqp.driver.consumer

import com.nummulus.amqp.driver.Channel

import com.rabbitmq.client.{Consumer => RabbitConsumer}

/**
 * A message consumer receives messages from a queue and proceses them.
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