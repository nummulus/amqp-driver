package com.nummulus.amqp.driver.api.consumer

/**
 * Response from a provider forwarded by a consumer.
 * 
 * @param body message contents
 */
case class AmqpConsumerResponse(body: String)
