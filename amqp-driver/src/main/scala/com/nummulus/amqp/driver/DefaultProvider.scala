package com.nummulus.amqp.driver

import org.slf4j.LoggerFactory

import com.nummulus.amqp.driver.akka.AmqpGuardianActor
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.nummulus.amqp.driver.provider.AkkaMessageConsumer

import _root_.akka.actor.Actor
import _root_.akka.actor.ActorRef
import _root_.akka.actor.ActorSystem
import _root_.akka.actor.Props
import _root_.akka.actor.Terminated

/**
 * Default provider implementation.
 */
private[driver] class DefaultProvider(channel: Channel, configuration: QueueConfiguration) extends AmqpProvider {
  private val logger = LoggerFactory.getLogger(getClass)
  private lazy val actorSystem = ActorSystem("AmqpDriver")
  private lazy val rootGuardian = actorSystem.actorOf(Props(classOf[RootGuardianActor], actorSystem, channel, configuration), configuration.queue + "RootGuardian")
  
  private val requestQueue = channel.queueDeclare(configuration.queue, configuration.durable, configuration.exclusive, configuration.autoDelete, null)
  logger.debug("Declared request queue: {}", configuration.queue)
  
  channel.basicQos(1)
  
  private var spent: Boolean = false
  
  def bind(actor: ActorRef) {
    if (spent) {
      throw new IllegalStateException("Cannot bind the same provider more than once.")
    }
    
    rootGuardian ! Start(actor)
    
    spent = true
  }
  
  def unbind() {
    rootGuardian ! Stop
  }
}

private case class Start(actor: ActorRef)
private case object Stop

private class RootGuardianActor(actorSystem: ActorSystem, channel: Channel, configuration: QueueConfiguration) extends Actor {
  private var consumerTag: Option[String] = None
  
  def receive = {
    case Start(actor) => {
      val guardianActor = actorSystem.actorOf(Props(classOf[AmqpGuardianActor], actor, channel, configuration), configuration.queue + "Guardian")
      context.watch(guardianActor)
      val callback = new AkkaMessageConsumer(channel, guardianActor)
      consumerTag = Some(channel.basicConsume(configuration.queue, configuration.autoAcknowledge, callback))
    }
    
    case Stop => {
      stop()
    }
    
    case _: Terminated => {
      stop()
    }
  }
  
  private def stop(): Unit = {
    consumerTag foreach { t => channel.basicCancel(t) }
  }
}