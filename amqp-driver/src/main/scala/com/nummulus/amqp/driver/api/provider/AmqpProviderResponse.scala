package com.nummulus.amqp.driver.api.provider

/**
 * Represents a response message from a provider to a consumer.
 * 
 * @param body message response
 * @param deliveryTag amqp request message identifier, is ignored if auto acknowledge is enabled
 */
case class AmqpProviderResponse(body: String, deliveryTag: Long = -1)
