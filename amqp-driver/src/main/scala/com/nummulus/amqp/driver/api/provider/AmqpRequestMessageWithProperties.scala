package com.nummulus.amqp.driver.api.provider

import com.nummulus.amqp.driver.MessageProperties

/**
 * Represents a message from AMQP.
 * 
 * @param body message contents
 * @param properties message properties
 * @param deliveryTag amqp message identifier
 */
private[driver] case class AmqpRequestMessageWithProperties(body: String, properties: MessageProperties, deliveryTag: Long)
