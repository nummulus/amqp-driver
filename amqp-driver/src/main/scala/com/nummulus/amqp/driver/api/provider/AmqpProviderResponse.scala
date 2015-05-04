package com.nummulus.amqp.driver.api.provider

/**
 * Response from a provider which will send it to a consumer's queue. 
 * 
 * @param body message response
 * @param deliveryTag amqp request message identifier, is ignored if auto acknowledge is enabled
 */
case class AmqpProviderResponse(body: String, deliveryTag: Long = -1)
