package com.nummulus.amqp.driver.akka

import java.nio.charset.StandardCharsets

import com.nummulus.amqp.driver.MessageProperties
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope

import akka.actor.ActorRef

/**
 * Akka message consumer.
 */
private[driver] class AkkaRabbitConsumer(channel: Channel, actor: ActorRef) extends DefaultConsumer(channel) {
   override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]) {
     val message = new String(body, StandardCharsets.UTF_8)
     val deliveryTag = envelope.getDeliveryTag
     
     actor ! AmqpQueueMessageWithProperties(message, MessageProperties(properties), deliveryTag)
   }
}