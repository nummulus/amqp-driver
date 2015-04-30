package com.nummulus.amqp.driver.akka

import com.nummulus.amqp.driver.Channel
import com.nummulus.amqp.driver.MessageConsumer
import com.rabbitmq.client.{Consumer => RabbitConsumer}

import akka.actor.ActorRef

class AkkaMessageConsumer(channel: Channel, actor: ActorRef) extends MessageConsumer {
  private lazy val consumer = new AkkaRabbitConsumer(channel.get, actor)
  
  override private[driver] def get: RabbitConsumer = consumer
}