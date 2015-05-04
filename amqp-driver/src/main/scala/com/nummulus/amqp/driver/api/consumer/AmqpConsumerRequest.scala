package com.nummulus.amqp.driver.api.consumer

import _root_.akka.actor.ActorRef

/**
 * Request for a consumer which will send it to a provider's queue.
 * 
 * @param body message contents
 * @param sender actor to send the response to if specified
 */
case class AmqpConsumerRequest(body: String, sender: Option[ActorRef] = None)
