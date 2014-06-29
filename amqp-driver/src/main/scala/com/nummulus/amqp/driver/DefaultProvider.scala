package com.nummulus.amqp.driver

import scala.concurrent.Await
import scala.concurrent.duration._

import org.slf4j.LoggerFactory

import com.nummulus.amqp.driver.akka.AmqpGuardianActorScope._
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.nummulus.amqp.driver.provider.AkkaMessageConsumer

import AmqpProvider._
import IdGenerators._
import _root_.akka.actor.ActorRef
import _root_.akka.actor.ActorSystem
import _root_.akka.actor.Props
import _root_.akka.util.Timeout

/**
 * Default provider implementation.
 */
private[driver] class DefaultProvider(
    channel: Channel, 
    configuration: QueueConfiguration, 
    generateId: IdGenerator = IdGenerators.random) extends AmqpProvider {
  
  private val logger = LoggerFactory.getLogger(getClass)
  private lazy val actorSystem = ActorSystem("AmqpDriver")
  
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
    val callback = new AkkaMessageConsumer(channel, guardianActor)
    val actor = createActor(guardianActor)
    initializeSynchronously(guardianActor, actor)
    
    spent = true
    consumerTag = Some(tag)
    channel.basicConsume(configuration.queue, configuration.autoAcknowledge, tag, callback)
  }
  
  def unbind(): Unit = consumerTag foreach { t => channel.basicCancel(t) }
  
  /**
   * Initializes the guardian actor synchronously.
   * 
   * This is needed because there is a theoretical possibility of a race condition.
   * If the Initialize message is sent, but for some reason takes a long time to
   * arrive, and if messages were already present on the AMQP queue, there is a
   * theoretical possibility that messages from the queue arrive at the AmqpGuardianActor
   * before the Initialize message does. In that case, the message will get lost.
   * This may or may not be a problem.
   * 
   * In order to prevent this issue, we make sure that Initialize is executed
   * synchronously.
   */
  private def initializeSynchronously(guardianActor: ActorRef, actor: ActorRef): Unit = {
    import _root_.akka.pattern.ask
    val duration = 2.seconds
    val future = (guardianActor ? Initialize(actor))(Timeout(duration))
    Await.ready(future, duration)
  }
}
