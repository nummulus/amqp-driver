package com.nummulus.amqp.driver.provider

import java.nio.charset.StandardCharsets

import com.nummulus.amqp.driver.MessageProperties
import com.nummulus.amqp.driver.api.provider.AmqpRequestMessageWithProperties
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
     
     actor ! AmqpRequestMessageWithProperties(message, MessageProperties(properties), deliveryTag)
   }
}