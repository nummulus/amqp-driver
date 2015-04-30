package com.nummulus.amqp.driver.api.consumer

import _root_.akka.actor.ActorRef

case class AmqpConsumerRequest(body: String, sender: Option[ActorRef] = None)
