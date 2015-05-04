package com.nummulus.amqp.driver

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
