package com.nummulus.amqp.driver.consumer

import com.nummulus.amqp.driver.Channel
import com.rabbitmq.client.{Consumer => RabbitConsumer}
import com.rabbitmq.client.QueueingConsumer

/**
 * Blocking message consumer.
 * 
 * Just a wrapper around [[com.rabbitmq.client.QueueingConsumer]].
 */
class BlockingMessageConsumer(channel: Channel) extends MessageConsumer {
  override def get: RabbitConsumer = new QueueingConsumer(channel.get)
}