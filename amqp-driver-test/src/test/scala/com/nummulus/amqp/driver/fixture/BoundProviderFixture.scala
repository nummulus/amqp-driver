package com.nummulus.amqp.driver.fixture

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

import com.nummulus.amqp.driver.Channel
import com.nummulus.amqp.driver.MessageProperties
import com.nummulus.amqp.driver.QueueDeclareOk
import com.nummulus.amqp.driver.akka.AmqpQueueMessageWithProperties
import com.nummulus.amqp.driver.api.provider.AmqpGuardianActor
import com.nummulus.amqp.driver.api.provider.Bind
import com.nummulus.amqp.driver.configuration.QueueConfiguration

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import akka.actor.Props

class BoundProviderFixture(autoAcknowledge: Boolean) extends MockitoSugar {
  val channel = mock[Channel]
  val declareOk = mock[QueueDeclareOk]
  
  when (channel.queueDeclare()) thenReturn declareOk
  when (channel.queueDeclare("requestQueue", durable = true, exclusive = false, autoDelete = false, null)) thenReturn declareOk
  
  val queueConfiguration = QueueConfiguration("requestQueue", durable = true, exclusive = false, autoDelete = false, autoAcknowledge = autoAcknowledge)
  
  
  implicit val system = ActorSystem("Test")
  val probe = TestProbe()
  
  val guardianActor = system.actorOf(Props(classOf[AmqpGuardianActor], channel, queueConfiguration, () => "random"), queueConfiguration.queue + "Guardian")
  guardianActor ! Bind(probe.ref)
  
  def sendMessage(deliveryTag: Int, body: String) {
    guardianActor ! AmqpQueueMessageWithProperties(body, MessageProperties(correlationId = "1e"), deliveryTag)
  }
}