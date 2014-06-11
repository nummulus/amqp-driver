package com.nummulus.amqp.driver.akka

import org.junit.runner.RunWith
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers
import org.scalatest.OneInstancePerTest
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import com.nummulus.amqp.driver.Channel
import com.nummulus.amqp.driver.MessageProperties
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.PoisonPill
import akka.testkit.TestActorRef
import akka.testkit.TestKit

@RunWith(classOf[JUnitRunner])
class AmqpGuardianActorTest extends TestKit(ActorSystem("test-system")) with FlatSpecLike with Matchers
    with MockitoSugar with BeforeAndAfterAll with OneInstancePerTest {
  
  val channel = mock[Channel]
  var ackCount = 0

  val someMessageBody = "some message"
  val someResponseBody = "some response"
  val someCorrelationId = "some correlation"
  val someReplyTo = "some Rabbit channel"
  val someDeliveryTag = 42L
  val someMessage = createMessage()
  val someResponse = createResponse()
  
  
  
  behavior of "AmqpGuardianActor with AutoAcknowledge"

  val autoAckGuardian = createGuardian(true)
  
  it should "pass on a message that appears on the channel" in {
    autoAckGuardian ! someMessage

    expectMsg(AmqpRequestMessage(someMessageBody, someDeliveryTag))
  }
  
  it should "never acknowledge a message, even when asked to (because RabbitMQ is responsible for that!)" in {
    autoAckGuardian ! someMessage
    autoAckGuardian ! Acknowledge(someDeliveryTag)

    verifyAcknowledgeNever(someDeliveryTag)
  }
  
  it should "publish a response to the channel" in {
    autoAckGuardian ! someMessage
    autoAckGuardian ! someResponse
    
    verifyPublishMessage(someReplyTo, someCorrelationId, someResponseBody)
  }
  
  it should "ignore a response to an unknown message" in {
    autoAckGuardian ! someResponse
    
    verifyPublishNothing()
  }
  
  it should "ignore the second response to a message" in {
    autoAckGuardian ! someMessage
    autoAckGuardian ! someResponse
    
    reset(channel)
    
    autoAckGuardian ! someResponse
    
    verifyPublishNothing()
  }
  
  it should "ignore responses if the guardian is already terminated" in {
    autoAckGuardian ! someMessage
    testActor ! PoisonPill
    
    reset(channel)
    
    autoAckGuardian ! someResponse

    verifyPublishNothing()
  }
  
  
  
  behavior of "AmqpGuardianActor without AutoAcknowledge"

  val noAckGuardian = createGuardian(false)
  
  it should "pass on a message that appears on the channel" in {
    noAckGuardian ! someMessage

    expectMsg(AmqpRequestMessage(someMessageBody, someDeliveryTag))
  }
  
  it should "acknowledge a message, but only when requested to" in {
    noAckGuardian ! someMessage
    verifyAcknowledgeNever(someDeliveryTag)

    noAckGuardian ! Acknowledge(someDeliveryTag)
    verifyAcknowledgeOnce(someDeliveryTag)
  }
  
  it should "not acknowledge twice" in {
    noAckGuardian ! someMessage
    noAckGuardian ! Acknowledge(someDeliveryTag)
    noAckGuardian ! Acknowledge(someDeliveryTag)
    
    verifyAcknowledgeOnce(someDeliveryTag)
  }
  
  it should "not acknowledge to the channel when the wrong delivery tag is acknowledged" in {
    val anotherDeliveryTag = someDeliveryTag + 1

    noAckGuardian ! someMessage
    noAckGuardian ! Acknowledge(anotherDeliveryTag)

    verifyAcknowledgeNever(someDeliveryTag)
    verifyAcknowledgeNever(anotherDeliveryTag)
  }
  
  it should "explicitly Nack a message when it receives Terminated" in {
    noAckGuardian ! someMessage
    testActor ! PoisonPill
    
    verify (channel, times(1)).basicNack(someDeliveryTag, false, true)
  }
  
  it should "publish a response to the channel" in {
    noAckGuardian ! someMessage
    noAckGuardian ! someResponse
    
    verifyAcknowledgeOnce(someDeliveryTag)
    verifyPublishMessage(someReplyTo, someCorrelationId, someResponseBody)
  }
  
  it should "ignore a response to an unknown message" in {
    noAckGuardian ! someResponse
    
    verifyAcknowledgeNever(someDeliveryTag)
    verifyPublishNothing()
  }
  
  it should "ignore the second response to a message" in {
    noAckGuardian ! someMessage
    noAckGuardian ! someResponse
    
    verifyAcknowledgeOnce(someDeliveryTag)
    reset(channel)
    
    noAckGuardian ! someResponse
    
    verifyAcknowledgeNever(someDeliveryTag)
    verifyPublishNothing()
  }
  
  it should "ignore responses if the guardian is already terminated" in {
    noAckGuardian ! someMessage
    testActor ! PoisonPill
    
    reset(channel)
    
    noAckGuardian ! someResponse

    verifyAcknowledgeOnce(someDeliveryTag)
    verifyPublishNothing()
  }
  
  
  
  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }
  
  private def verifyAcknowledgeNever(deliveryTag: Long): Unit = {
    verify (channel, never()).basicAck(deliveryTag, true)
    verify (channel, never()).basicAck(deliveryTag, false)
  }
  
  private def verifyAcknowledgeOnce(deliveryTag: Long): Unit = {
    verify (channel, times(1)).basicAck(deliveryTag, false)
    verify (channel, never()).basicAck(deliveryTag, true)
  }
  
  private def verifyPublishNothing(): Unit = {
    verify (channel, never()).basicPublish(anyString, anyString, any[MessageProperties], any[Array[Byte]])
  }

  private def verifyPublishMessage(replyTo: String, correlationId: String, body: String): Unit = {
    verify (channel, times(1)).basicPublish("", replyTo, MessageProperties(correlationId = correlationId), body.getBytes)
  }
  
  private def createGuardian(autoAcknowledge: Boolean): ActorRef = {
    val name = if (autoAcknowledge) "AutoAckTestGuardian" else "NoAckTestGuardian"
    val configuration = mock[QueueConfiguration]
    when (configuration.autoAcknowledge) thenReturn autoAcknowledge
    TestActorRef(new AmqpGuardianActor(testActor, channel, "some-unique-id-string", configuration))
  }
  
  private def createMessage(messageBody: String = someMessageBody, correlationId: String = someCorrelationId, replyTo: String = someReplyTo, deliveryTag: Long = someDeliveryTag): AmqpRequestMessageWithProperties =
    AmqpRequestMessageWithProperties(someMessageBody, MessageProperties(correlationId = correlationId, replyTo = replyTo), someDeliveryTag)
  
  private def createResponse(messageBody: String = someResponseBody, deliveryTag: Long = someDeliveryTag): AmqpResponseMessage =
    AmqpResponseMessage(someResponseBody, someDeliveryTag)
}
