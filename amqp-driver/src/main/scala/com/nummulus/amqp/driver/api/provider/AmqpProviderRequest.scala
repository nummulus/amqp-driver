package com.nummulus.amqp.driver.api.provider

/**
 * Request from the provider's queue sent by a consumer.
 * 
 * @param body message contents
 * @param deliveryTag amqp message identifier
 */
case class AmqpProviderRequest(body: String, deliveryTag: Long)
