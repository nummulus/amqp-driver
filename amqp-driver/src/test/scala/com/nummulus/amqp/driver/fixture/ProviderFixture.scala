package com.nummulus.amqp.driver.fixture

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import com.nummulus.amqp.driver.Channel
import com.nummulus.amqp.driver.QueueDeclareOk
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.nummulus.amqp.driver.DefaultProvider
import com.nummulus.amqp.driver.AmqpProvider
import akka.actor.ActorSystem
import akka.testkit.TestActorRef

class ProviderFixture(queueName: String = "requestQueue")(implicit system: ActorSystem) extends MockitoSugar {
  val channel = mock[Channel]
  val declareOk = mock[QueueDeclareOk]
  
  when (channel.queueDeclare) thenReturn declareOk
  when (channel.queueDeclare(queueName, true, false, false, null)) thenReturn declareOk
  
  val queueConfiguration = QueueConfiguration(queueName, true, false, false, true)
  
  val provider: AmqpProvider = new DefaultProvider(system, channel, queueConfiguration)
}