package com.nummulus.amqp.driver

import _root_.akka.actor.ActorRef


/**
 * A provider listens to a well-known queue and provides a certain service.
 */
trait AmqpProvider {
  def bind(actor: ActorRef)
  def unbind()
  def handleNextDelivery()
}