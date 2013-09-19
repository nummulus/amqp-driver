package com.nummulus.amqp.driver.consumer

import com.nummulus.amqp.driver.MessageProperties

/**
 * Represents a message from the broker.
 */
case class Delivery(properties: MessageProperties, body: Array[Byte], deliveryTag: Long)