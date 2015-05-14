package com.nummulus.amqp.driver

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import org.mockito.Mockito._
import org.mockito.{Matchers => MockitoMatchers}
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers

import com.nummulus.amqp.driver.akka.AkkaMessageConsumer
import com.nummulus.amqp.driver.akka.AmqpQueueMessageWithProperties
import com.nummulus.amqp.driver.api.consumer.AmqpConsumerRequest
import com.nummulus.amqp.driver.api.consumer.AmqpConsumerRequestTimedOut
import com.nummulus.amqp.driver.api.consumer.AmqpConsumerResponse
import com.nummulus.amqp.driver.api.consumer.RequestTimedOut
import com.nummulus.amqp.driver.fixture.AkkaConsumerFixture

import _root_.akka.actor.ActorRef
import _root_.akka.actor.ActorSystem
import _root_.akka.testkit.ImplicitSender
import _root_.akka.testkit.TestActorRef
import _root_.akka.testkit.TestKit

class DefaultConsumerTest extends TestKit(ActorSystem("test-system"))
    with ImplicitSender
    with FlatSpecLike
    with Matchers {
  
  behavior of "DefaultAkkaConsumer"
  
  it should "declare a response queue at construction time" in new AkkaConsumerFixture {
    verify (channel).queueDeclare()
  }
  
  it should "declare a request queue at construction time" in new AkkaConsumerFixture {
    verify (channel).queueDeclare("requestQueue", durable = true, exclusive = false, autoDelete = false, null)
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
    
    consumer ! AmqpQueueMessageWithProperties(
      "Camembert",
      MessageProperties(
        correlationId = someCorrelationId,
        replyTo = "nowhere"),
      1)
    expectMsg(AmqpConsumerResponse("Camembert"))
  }
  
  it should "return nothing if no sender is specified" in new AkkaConsumerFixture {
    consumer ! AmqpConsumerRequest("boo!", None)
    
    consumer ! AmqpQueueMessageWithProperties(
      "Cowers in fear!",
      MessageProperties(
        correlationId = someCorrelationId,
        replyTo = "nowhere"),
      1)
    
    expectNoMsg(100.millis) // timeout is enough since consumer is synchronous
  }

  it should "schedule a time-out message if a time-out is specified" in new AkkaConsumerFixture {
    private val consumerWithTimeOut: TestActorRef[DefaultConsumer] = consumerWithTimeOut(100.millis)

    consumerWithTimeOut ! AmqpConsumerRequest("request cheese", Some(self))

    verify (scheduler).scheduleOnce(
      MockitoMatchers.eq(100.millis),
      MockitoMatchers.eq(consumerWithTimeOut),
      MockitoMatchers.eq(RequestTimedOut(someCorrelationId))
    )(
      MockitoMatchers.any(classOf[ExecutionContext]),
      MockitoMatchers.any(classOf[ActorRef])
    )
  }

  it should "not schedule a time-out message if the time-out is infinite" in new AkkaConsumerFixture {
    private val consumerWithTimeOut: TestActorRef[DefaultConsumer] = consumerWithTimeOut(100.millis)

    consumerWithTimeOut ! AmqpConsumerRequest("request cheese")

    verifyZeroInteractions (scheduler)
  }

  it should "return an AmqpConsumerRequestTimedOut if a time-out is received before the response" in new AkkaConsumerFixture {
    private val consumerWithTimeOut: TestActorRef[DefaultConsumer] = consumerWithTimeOut(100.millis)

    consumerWithTimeOut ! AmqpConsumerRequest("request cheese", Some(self))

    consumerWithTimeOut ! RequestTimedOut(someCorrelationId)

    expectMsg(AmqpConsumerRequestTimedOut)
  }

  it should "ignore the time-out if the response is received before the time-out" in new AkkaConsumerFixture {
    private val consumerWithTimeOut: TestActorRef[DefaultConsumer] = consumerWithTimeOut(100.millis)

    consumerWithTimeOut ! AmqpConsumerRequest("request cheese", Some(self))

    consumerWithTimeOut ! AmqpQueueMessageWithProperties(
      "Camembert",
      MessageProperties(
        correlationId = someCorrelationId,
        replyTo = "nowhere"),
      1)

    expectMsg(AmqpConsumerResponse("Camembert"))

    consumerWithTimeOut ! RequestTimedOut(someCorrelationId)

    expectNoMsg(100.millis)
  }
}
