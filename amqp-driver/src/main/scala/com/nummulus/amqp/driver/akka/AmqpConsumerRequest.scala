package com.nummulus.amqp.driver.akka

import _root_.akka.actor.ActorRef

case class AmqpConsumerRequest(body: String, sender: Option[ActorRef] = None)
