package com.nummulus.amqp.driver.api.provider

/**
 * Represents a message from AMQP.
 * 
 * @param body message contents
 * @param deliveryTag amqp message identifier
 */
case class AmqpRequestMessage(body: String, deliveryTag: Long)
