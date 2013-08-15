package com.nummulus.amqp.driver

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import com.typesafe.config.ConfigFactory
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.nummulus.amqp.driver.consumer.MessageConsumer
import com.nummulus.amqp.driver.consumer.BlockingMessageConsumer

@RunWith(classOf[JUnitRunner])
class DefaultConsumerTest extends FlatSpec with Matchers with MockitoSugar with OneInstancePerTest {
  behavior of "DefaultConsumer"
  
  it should "declare a response queue at construction time" in {
    verify (channel).queueDeclare
  }
  
  it should "declare a request queue at construction time" in {
    verify (channel).queueDeclare("requestQueue", true, false, false, null)
  }
  
  it should "tie a consumer to the response queue" in {
    verify (channel).basicConsume("generated-queue-name", true, messageConsumer)
  }
  
  // Test fixture
  val channel = mock[Channel]
  val declareOk = mock[QueueDeclareOk]
  val messageConsumer = mock[MessageConsumer]
  
  when (channel.queueDeclare) thenReturn declareOk
  when (channel.queueDeclare("requestQueue", true, false, false, null)) thenReturn declareOk
  when (declareOk.getQueue) thenReturn "generated-queue-name"
  
  val queueConfiguration = QueueConfiguration("requestQueue", true, false, false, true)
  
  val consumer = new DefaultConsumer(channel, queueConfiguration, messageConsumer)
}
