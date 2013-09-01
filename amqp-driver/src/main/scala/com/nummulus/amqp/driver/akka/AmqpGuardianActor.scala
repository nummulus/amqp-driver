package com.nummulus.amqp.driver.akka

import com.nummulus.amqp.driver.Channel

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Terminated

/**
 * Entry point for AMQP messages to enter the Akka world.
 * 
 * Monitors the actor which it sends messages to. If the monitored actor dies,
 * all unacknowledged messages will be requeued.
 */
private[driver] class AmqpGuardianActor(actor: ActorRef, channel: Channel, autoAcknowledge: Boolean) extends Actor {
  private var unprocessed = Set[Long]()
  
  context.watch(actor)
  
  override def receive = {
    /**
     * Handles an incoming message from the queue.
     */
    case AmqpMessage(message, deliveryTag) => {
      unprocessed += deliveryTag
      actor ! message
      
      if (autoAcknowledge) channel.basicAck(deliveryTag, false)
    }
    
    /**
     * Handles an acknowledge message from another actor.
     */
    case Acknowledge(deliveryTag) => unprocessed -= deliveryTag
    
    /**
     * Handles a terminate message from the watched actor by requeueing all unacknowledged messages.
     */
    case Terminated => unprocessed foreach (channel.basicNack(_, false, true))
  }
}