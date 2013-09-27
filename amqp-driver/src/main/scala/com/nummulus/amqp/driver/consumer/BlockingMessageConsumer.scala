package com.nummulus.amqp.driver.consumer

import com.nummulus.amqp.driver.Channel
import com.nummulus.amqp.driver.MessageConsumer
import com.nummulus.amqp.driver.MessageProperties

import com.rabbitmq.client.{Consumer => RabbitConsumer}
import com.rabbitmq.client.QueueingConsumer

/**
 * Blocking message consumer.
 * 
 * Just a wrapper around [[com.rabbitmq.client.QueueingConsumer]].
 */
class BlockingMessageConsumer(channel: Channel) extends MessageConsumer {
  private lazy val consumer = new QueueingConsumer(channel.get)
  
  override private[driver] def get: RabbitConsumer = consumer
  
  /**
   * Blocks until a message appears and returns it as a delivery.
   */
  def nextDelivery: Delivery = {
    val delivery = consumer.nextDelivery()
    val properties = MessageProperties(delivery.getProperties())
    
    Delivery(properties, delivery.getBody(), delivery.getEnvelope.getDeliveryTag)
  }
}