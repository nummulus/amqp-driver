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
  logger.debug("Declared request queue: {}", configuration.queue)
  
  channel.basicQos(1)
  
  private var consumerTag: Option[String] = None
  private var spent: Boolean = false
  
  def bind(actor: ActorRef) {
    if (spent) {
      throw new IllegalStateException("Cannot bind the same provider more than once.")
    }
    
    val guardianActor = actorSystem.actorOf(Props(classOf[AmqpGuardianActor], actor, channel, configuration), configuration.queue + "Guardian")
    val callback = new AkkaMessageConsumer(channel, guardianActor)
    
    spent = true
    consumerTag = Some(channel.basicConsume(configuration.queue, configuration.autoAcknowledge, callback))
  }
  
  def unbind() {
    consumerTag foreach { t => channel.basicCancel(t) }
  }
}