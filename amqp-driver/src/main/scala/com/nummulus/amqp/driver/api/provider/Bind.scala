package com.nummulus.amqp.driver.api.provider

import akka.actor.ActorRef

/**
 * Request to bind the specified actor to the AmqpGuardianActor.
 */
case class Bind(actor: ActorRef)
