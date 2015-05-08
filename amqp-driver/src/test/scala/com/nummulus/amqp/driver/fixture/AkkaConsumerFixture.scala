package com.nummulus.amqp.driver.fixture

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

import com.nummulus.amqp.driver.Channel
import com.nummulus.amqp.driver.DefaultConsumer
import com.nummulus.amqp.driver.QueueDeclareOk
import com.nummulus.amqp.driver.akka.AkkaMessageConsumer
import com.nummulus.amqp.driver.configuration.QueueConfiguration

import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.TestActorRef

class AkkaConsumerFixture(implicit system: ActorSystem) extends MockitoSugar {
  val channel = mock[Channel]
  val declareOk = mock[QueueDeclareOk]
  val messageConsumer = mock[AkkaMessageConsumer]
  val someCorrelationId = "4"
  val correlationIdGenerator = () => someCorrelationId
  
  when (channel.queueDeclare()) thenReturn declareOk
  when (channel.queueDeclare("requestQueue", true, false, false, null)) thenReturn declareOk
  when (declareOk.getQueue) thenReturn "generated-queue-name"
  
  val queueConfiguration = QueueConfiguration("requestQueue", true, false, false, true)
  
  val consumer = TestActorRef[DefaultConsumer](Props(classOf[DefaultConsumer], channel, queueConfiguration, correlationIdGenerator))
}
