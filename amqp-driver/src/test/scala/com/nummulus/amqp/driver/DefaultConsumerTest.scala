package com.nummulus.amqp.driver

import scala.concurrent.ExecutionContext.Implicits.global
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.exceptions.TestFailedException
import org.scalatest.junit._
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.Span
import org.scalatest.time.Millis
import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import com.typesafe.config.ConfigFactory
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.nummulus.amqp.driver.consumer.MessageConsumer
import com.nummulus.amqp.driver.consumer.BlockingMessageConsumer
import com.nummulus.amqp.driver.consumer.Delivery
import com.nummulus.amqp.driver.consumer.CorrelationIdGenerator

@RunWith(classOf[JUnitRunner])
class DefaultConsumerTest extends FlatSpec with Matchers with MockitoSugar with OneInstancePerTest with ScalaFutures with OptionValues {
  behavior of "DefaultConsumer"
  
  it should "declare a response queue at construction time" in {
    verify (channel).queueDeclare
  }
  
  it should "declare a request queue at construction time" in {
    verify (channel).queueDeclare("requestQueue", true, false, false, null)
  }
  
  it should "tie a consumer to the response queue at construction time" in {
    verify (channel).basicConsume("generated-queue-name", true, messageConsumer)
  }
  
  it should "publish a message when calling ask" in {
    val exchangeCaptor = ArgumentCaptor.forClass(classOf[String])
    val routingKeyCaptor = ArgumentCaptor.forClass(classOf[String])
    val propsCaptor = ArgumentCaptor.forClass(classOf[MessageProperties])
    val messageCaptor = ArgumentCaptor.forClass(classOf[Array[Byte]])
    val consumerWithDefaultCorrelationIdGenerator = new DefaultConsumer(channel, queueConfiguration, messageConsumer)
    
    consumerWithDefaultCorrelationIdGenerator.ask("Cheese")
    
    verify (channel).basicPublish(exchangeCaptor.capture(), routingKeyCaptor.capture(), propsCaptor.capture(), messageCaptor.capture())
    
    exchangeCaptor.getValue should be ("")
    routingKeyCaptor.getValue should be ("requestQueue")
    messageCaptor.getValue should be ("Cheese".getBytes)
    
    val props = propsCaptor.getValue
    props.replyTo should be ("generated-queue-name")
    props.correlationId should (fullyMatch regex(uuidPattern))
  }
  
  it should "publish a message and receive a response" in {
    val properties = MessageProperties(correlationId = "4")
    when (messageConsumer.nextDelivery) thenReturn Delivery(properties, "Gromit".getBytes)
    
    val response = consumer.ask("Cheese")
    whenReady (response) { r =>
      r should be ("Gromit")
    }
  }
  
  it should "discard messages with the wrong correlationId" in {
    implicit val patienceConfig = PatienceConfig(timeout = Span(100, Millis), interval = Span(5, Millis))
    
    val properties = MessageProperties(correlationId = "wrong-id")
    when (messageConsumer.nextDelivery) thenReturn Delivery(properties, "Gromit".getBytes)
    
    val response = consumer.ask("Cheese")
    val thrown = intercept[TestFailedException] {
      whenReady (response) { r =>
        // Empty because we expect a timeout
      }
    }
    
    thrown.message.value should include ("A timeout occurred waiting for a future to complete")
  }
  
  // Test fixture
  val channel = mock[Channel]
  val declareOk = mock[QueueDeclareOk]
  val messageConsumer = mock[BlockingMessageConsumer]
  val correlationIdGenerator = mock[CorrelationIdGenerator]
  
  when (channel.queueDeclare) thenReturn declareOk
  when (channel.queueDeclare("requestQueue", true, false, false, null)) thenReturn declareOk
  when (declareOk.getQueue) thenReturn "generated-queue-name"
  when (correlationIdGenerator.generate) thenReturn "4"
  
  val queueConfiguration = QueueConfiguration("requestQueue", true, false, false, true)
  
  val consumer = new DefaultConsumer(channel, queueConfiguration, messageConsumer, correlationIdGenerator)
  
  val uuidPattern = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
}
