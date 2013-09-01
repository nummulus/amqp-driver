package com.nummulus.amqp.driver.akka

/**
 * Represents a message from AMQP.
 * 
 * @param body message contents
 * @param deliveryTag amqp message identifier
 */
case class AmqpMessage(body: String, deliveryTag: Long)