package com.nummulus.amqp.driver.fixture

import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

import com.nummulus.amqp.driver.Channel
import com.nummulus.amqp.driver.DefaultProvider
import com.nummulus.amqp.driver.MessageConsumer
import com.nummulus.amqp.driver.QueueDeclareOk
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.Envelope

import akka.actor.ActorSystem
import akka.testkit.TestProbe

class BoundProviderFixture(autoAcknowledge: Boolean) extends MockitoSugar {
  val channel = mock[Channel]
  val declareOk = mock[QueueDeclareOk]
  
  when (channel.queueDeclare) thenReturn declareOk
  when (channel.queueDeclare("requestQueue", true, false, false, null)) thenReturn declareOk
  
  val queueConfiguration = QueueConfiguration("requestQueue", true, false, false, autoAcknowledge)
  
  val provider = new DefaultProvider(channel, queueConfiguration)
  
  implicit val system = ActorSystem("Test")
  val probe = TestProbe()
  
  provider.bind(probe.ref)
  
  val queueCaptor = ArgumentCaptor.forClass(classOf[String])
  val autoAcknowledgeCaptor = ArgumentCaptor.forClass(classOf[Boolean])
  val callbackCaptor = ArgumentCaptor.forClass(classOf[MessageConsumer])
  
  verify (channel).basicConsume(queueCaptor.capture(), autoAcknowledgeCaptor.capture(), callbackCaptor.capture())
  
  val consumer = callbackCaptor.getValue.get
  
  def sendMessage(deliveryTag: Int, body: String) {
    val envelope = new Envelope(deliveryTag, false, "", "routing")
    val properties = new BasicProperties().builder.correlationId("1e").build
    consumer.handleDelivery("", envelope, properties, body.getBytes)
  }
}