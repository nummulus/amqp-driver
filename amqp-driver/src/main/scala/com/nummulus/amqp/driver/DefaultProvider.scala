package com.nummulus.amqp.driver

import org.slf4j.LoggerFactory

import com.nummulus.amqp.driver.akka.AmqpGuardianActor
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.nummulus.amqp.driver.provider.AkkaMessageConsumer

import _root_.akka.actor.ActorRef
import _root_.akka.actor.ActorSystem
import _root_.akka.actor.Props

/**
 * Default provider implementation.
 */
private[driver] class DefaultProvider(channel: Channel, configuration: QueueConfiguration) extends AmqpProvider {
  private val logger = LoggerFactory.getLogger(getClass)
  private lazy val actorSystem = ActorSystem("AmqpDriver")
  
  private val requestQueue = channel.queueDeclare(configuration.queue, configuration.durable, configuration.exclusive, configuration.autoDelete, null)
  logger.debug("Declared request queue: {}", requestQueue.getQueue)
  
  channel.basicQos(1)
  
  var actor: Option[ActorRef] = None
  
  def bind(actor: ActorRef) {
    val guardianActor = actorSystem.actorOf(Props(classOf[AmqpGuardianActor], actor, channel, configuration.autoAcknowledge), actor.path + "guardian")
    val callback = new AkkaMessageConsumer(channel, guardianActor)
    
    channel.basicConsume(requestQueue.getQueue, configuration.autoAcknowledge, callback)
    
    this.actor = Some(actor)
  }
  
  def unbind() {
    this.actor = None
  }
}