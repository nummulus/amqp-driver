package com.nummulus.amqp.driver.akka

/**
 * Represents a response message from a provider to a consumer.
 * 
 * @param body message response
 * @param deliveryTag amqp request message identifier, is ignored if auto acknowledge is enabled
 */
case class AmqpResponseMessage(body: String, deliveryTag: Long = -1)