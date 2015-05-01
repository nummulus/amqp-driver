package com.nummulus.amqp.driver.akka

import com.nummulus.amqp.driver.MessageProperties

/**
 * Represents a message from AMQP.
 * 
 * @param body message contents
 * @param properties message properties
 * @param deliveryTag amqp message identifier
 */
private[driver] case class AmqpQueueMessageWithProperties(body: String, properties: MessageProperties, deliveryTag: Long)
