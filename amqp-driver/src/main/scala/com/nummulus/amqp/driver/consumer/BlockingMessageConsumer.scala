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
    val props = delivery.getProperties()
    
    val properties = MessageProperties(
        contentType = props.getContentType(),
        contentEncoding = props.getContentEncoding(),
        headers = null,
        deliveryMode = integer2int(props.getDeliveryMode, 1),
        priority = integer2int(props.getPriority(), 0),
        correlationId = props.getCorrelationId(),
        replyTo = props.getReplyTo(),
        expiration = props.getExpiration(),
        messageId = props.getMessageId(),
        userId = props.getUserId(),
        appId = props.getAppId(),
        cluserId = props.getClusterId())
    
    Delivery(properties, delivery.getBody())
  }
  
  /**
   * Returns the value if it's not null, returns the default value otherwise.
   */
  private def integer2int(value: Integer, defaultValue: Int): Int = if (value != null) value else defaultValue
}