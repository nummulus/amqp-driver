package com.nummulus.amqp.driver.akka

import org.scalatest.FlatSpecLike
import org.scalatest.Matchers
import org.scalatest.OneInstancePerTest
import org.scalatest.mock.MockitoSugar

import com.nummulus.amqp.driver.MessageProperties
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Envelope

import akka.actor.ActorSystem
import akka.testkit.TestKit

class AkkaRabbitConsumerTest extends TestKit(ActorSystem("test-system")) with FlatSpecLike with Matchers with MockitoSugar with OneInstancePerTest {
  val SomeDeliveryTag = 1337
  val SomeMessageBody = "some message body"
  val SomeEnvelope = new Envelope(SomeDeliveryTag, false, "", "")
  val SomeProperties = new AMQP.BasicProperties()
  val channel = mock[Channel]
  
  behavior of "AkkaRabbitConsumer"
  
  it should "pass on an AmqpRequestMessageWithProperties" in {
    val consumer = new AkkaRabbitConsumer(channel, testActor)
    
    consumer.handleDelivery("", SomeEnvelope, SomeProperties, SomeMessageBody.getBytes)
    
    expectMsg(AmqpQueueMessageWithProperties(SomeMessageBody, MessageProperties(SomeProperties), SomeDeliveryTag))
  }
}
