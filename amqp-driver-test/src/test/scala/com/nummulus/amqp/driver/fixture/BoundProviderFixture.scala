package com.nummulus.amqp.driver.fixture

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

import com.nummulus.amqp.driver.Channel
import com.nummulus.amqp.driver.DefaultProvider
import com.nummulus.amqp.driver.MessageProperties
import com.nummulus.amqp.driver.QueueDeclareOk
import com.nummulus.amqp.driver.akka.AmqpQueueMessageWithProperties
import com.nummulus.amqp.driver.configuration.QueueConfiguration

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import akka.actor.ActorRef

class BoundProviderFixture(autoAcknowledge: Boolean) extends MockitoSugar {
  val channel = mock[Channel]
  val declareOk = mock[QueueDeclareOk]
  
  when (channel.queueDeclare) thenReturn declareOk
  when (channel.queueDeclare("requestQueue", true, false, false, null)) thenReturn declareOk
  
  val queueConfiguration = QueueConfiguration("requestQueue", true, false, false, autoAcknowledge)
  
  
  implicit val system = ActorSystem("Test")
  val probe = TestProbe()
  val provider = new DefaultProvider(system, channel, queueConfiguration)
  
  var guardianActor: ActorRef = null
  provider.bind(actor => {
    guardianActor = actor
    probe.ref
  })
  
  def sendMessage(deliveryTag: Int, body: String) {
    guardianActor ! AmqpQueueMessageWithProperties(body, MessageProperties(correlationId = "1e"), deliveryTag)
  }
}