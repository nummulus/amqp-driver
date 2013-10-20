package com.nummulus.amqp.driver.consumer

import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import com.nummulus.amqp.driver.Channel
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.QueueingConsumer
import com.rabbitmq.client.QueueingConsumer.{Delivery => RabbitDelivery}
import com.rabbitmq.client.Envelope
import com.nummulus.amqp.driver.MessageProperties

@RunWith(classOf[JUnitRunner])
class BlockingMessageConsumerTest extends FlatSpec with Matchers with MockitoSugar {
  val delivery = mock[RabbitDelivery]
  val envelope = mock[Envelope]
  val channel = mock[Channel]
  val queueingConsumer = mock[QueueingConsumer]
  
  val SomeDeliveryTag = 1337
  val SomeMessageBody = "some message body".getBytes
  val SomeProperties = new AMQP.BasicProperties()
  
  when (queueingConsumer.nextDelivery) thenReturn delivery
  when (delivery.getProperties) thenReturn SomeProperties
  when (delivery.getBody) thenReturn SomeMessageBody
  when (delivery.getEnvelope) thenReturn envelope
  when (envelope.getDeliveryTag) thenReturn SomeDeliveryTag

  behavior of "BlockingMessageConsumer"
  
  it should "pass on a Delivery" in {
    val consumer = new BlockingMessageConsumer(channel)(_ => queueingConsumer)
    val result = consumer.nextDelivery
    result should be (Delivery(MessageProperties(SomeProperties), SomeMessageBody, SomeDeliveryTag))
  }
}
