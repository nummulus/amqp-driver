package com.nummulus.amqp.driver

import org.slf4j.LoggerFactory

import com.nummulus.amqp.driver.api.provider.AmqpGuardianActorScope._
import com.nummulus.amqp.driver.configuration.QueueConfiguration

import AmqpProvider._
import IdGenerators._
import _root_.akka.actor.ActorRef
import _root_.akka.actor.ActorSystem
import _root_.akka.actor.Props

/**
 * Default provider implementation.
 */
private[driver] class DefaultProvider(
    actorSystem: ActorSystem,
    channel: Channel, 
    configuration: QueueConfiguration, 
    generateId: IdGenerator = IdGenerators.random) extends AmqpProvider {
  
  private val logger = LoggerFactory.getLogger(getClass)
  
  private val requestQueue = channel.queueDeclare(configuration.queue, configuration.durable, configuration.exclusive, configuration.autoDelete, null)
  logger.debug("Declared request queue: {}", configuration.queue)
  
  channel.basicQos(1)
  
  private var consumerTag: Option[String] = None
  private var spent: Boolean = false
  
  def bind(actor: ActorRef): Unit = bind(_ => actor)
  
  def bind(createActor: ActorFactory): Unit = {
    if (spent) {
      throw new IllegalStateException("Cannot bind the same provider more than once.")
    }
    
    val tag = generateId()
    val guardianActor = actorSystem.actorOf(Props(classOf[AmqpGuardianActor], channel, tag, configuration), configuration.queue + "Guardian")
    
    val actor = createActor(guardianActor)
    guardianActor ! Initialize(actor)
    
    spent = true
    consumerTag = Some(tag)
  }
  
  def unbind(): Unit = consumerTag foreach { t => channel.basicCancel(t) }
}
