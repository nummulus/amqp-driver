package com.nummulus.amqp.driver

import org.junit.runner.RunWith
import org.mockito.{Matchers => MockitoMatchers}
import org.mockito.Mockito._
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import com.nummulus.amqp.driver.api.consumer.AmqpConsumerResponse
import com.nummulus.amqp.driver.api.consumer.AmqpConsumerRequest
import com.nummulus.amqp.driver.api.provider.AmqpRequestMessageWithProperties
import com.nummulus.amqp.driver.fixture.AkkaConsumerFixture
import com.nummulus.amqp.driver.provider.AkkaMessageConsumer

import _root_.akka.actor.ActorSystem
import _root_.akka.testkit.ImplicitSender
import _root_.akka.testkit.TestKit

@RunWith(classOf[JUnitRunner])
class DefaultAkkaConsumerTest extends TestKit(ActorSystem("test-system"))
    with ImplicitSender
    with FlatSpecLike
    with Matchers {
  
  behavior of "DefaultAkkaConsumer"
  
  it should "declare a response queue at construction time" in new AkkaConsumerFixture {
    verify (channel).queueDeclare
  }
  
  it should "declare a request queue at construction time" in new AkkaConsumerFixture {
    verify (channel).queueDeclare("requestQueue", true, false, false, null)
  }
  
  it should "tie a consumer to the response queue at construction time" in new AkkaConsumerFixture {
    verify (channel).basicConsume(
      MockitoMatchers.eq("generated-queue-name"),
      MockitoMatchers.eq(true),
      MockitoMatchers.eq(someCorrelationId), 
      MockitoMatchers.any(classOf[AkkaMessageConsumer]))
  }
  
  it should "publish a message when receiving an AmqpConsumerRequest" in new AkkaConsumerFixture {
    consumer ! AmqpConsumerRequest("publish me", None)
    
    verify (channel).basicPublish(
      MockitoMatchers.eq(""),
      MockitoMatchers.eq("requestQueue"),
      MockitoMatchers.any(classOf[MessageProperties]),
      MockitoMatchers.eq("publish me".getBytes))
  }
  
  it should "return an AmqpConsumerResponse if a sender is specified" in new AkkaConsumerFixture {
    consumer ! AmqpConsumerRequest("request cheese", Some(self))
    
    consumer ! AmqpRequestMessageWithProperties(
      "Camembert",
      MessageProperties(
        correlationId = someCorrelationId,
        replyTo = "nowhere"),
      1)
    expectMsg(AmqpConsumerResponse("Camembert"))
  }
  
  it should "return nothing if no sender is specified" in new AkkaConsumerFixture {
    consumer ! AmqpConsumerRequest("boo!", None)
    
    consumer ! AmqpRequestMessageWithProperties(
      "Cowers in fear!",
      MessageProperties(
        correlationId = someCorrelationId,
        replyTo = "nowhere"),
      1)
    
    import scala.concurrent.duration._
    expectNoMsg(100.millis) // timeout is enough since consumer is synchronous
  }
}
