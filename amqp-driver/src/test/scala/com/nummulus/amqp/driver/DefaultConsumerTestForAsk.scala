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
import org.mockito.Mockito._
import com.nummulus.amqp.driver.consumer.Delivery
import com.nummulus.amqp.driver.fixture.ConsumerFixture

@RunWith(classOf[JUnitRunner])
class DefaultConsumerTestForAsk extends FlatSpec with Matchers with MockitoSugar with ScalaFutures with OptionValues {
  behavior of "DefaultConsumer#ask"
  
  it should "publish a message when calling ask" in new ConsumerFixture {
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
  
  it should "publish a message and receive a response" in new ConsumerFixture {
    val properties = MessageProperties(correlationId = "4")
    when (messageConsumer.nextDelivery) thenReturn Delivery(properties, "Gromit".getBytes, 1337)
    
    val response = consumer.ask("Cheese")
    whenReady (response) { r =>
      r should be ("Gromit")
    }
  }
  
  it should "discard messages with the wrong correlationId" in new ConsumerFixture {
    implicit val patienceConfig = PatienceConfig(timeout = Span(100, Millis), interval = Span(5, Millis))
    
    val properties = MessageProperties(correlationId = "wrong-id")
    when (messageConsumer.nextDelivery) thenReturn Delivery(properties, "Gromit".getBytes, 1337)
    
    val response = consumer.ask("Cheese")
    val thrown = intercept[TestFailedException] {
      whenReady (response) { r =>
        // Empty because we expect a timeout
      }
    }
    
    thrown.message.value should include ("A timeout occurred waiting for a future to complete")
  }
  
  val uuidPattern = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
}