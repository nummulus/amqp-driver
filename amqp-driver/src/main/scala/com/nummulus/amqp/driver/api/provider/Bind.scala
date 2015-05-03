package com.nummulus.amqp.driver.api.provider

import akka.actor.ActorRef

/**
 * Request to bind the specified actor to the AmqpGuardianActor.
 */
private[driver] case class Bind(actor: ActorRef)
