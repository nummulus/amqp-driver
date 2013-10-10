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
import akka.actor.Terminated
import akka.testkit.TestActorRef
import akka.testkit.TestKit

@RunWith(classOf[JUnitRunner])
class AmqpGuardianActorTest extends TestKit(ActorSystem("test-system")) with FlatSpecLike with Matchers
    with MockitoSugar with BeforeAndAfterAll with OneInstancePerTest {
  
  val channel = mock[Channel]
  var ackCount = 0

  val someMessageBody = "some message"
  val someReplyTo = "some Rabbit channel"
  val someDeliveryTag = 42L
  val someMessage = createMessage()
  
  
  
  behavior of "AmqpGuardianActor with AutoAcknowledge"

  val autoAckGuardian = createGuardian(true)
  
  it should "pass on a message that appears on the channel" in {
    autoAckGuardian ! someMessage

    expectMsg(AmqpRequestMessage(someMessageBody, someDeliveryTag))
  }
  
  it should "never acknowledge a message, even when asked to (because RabbitMQ is responsible for that!)" in {
    autoAckGuardian ! someMessage
    autoAckGuardian ! Acknowledge(someDeliveryTag)

    acknowledgeNever(someDeliveryTag)
  }
  
  
  
  behavior of "AmqpGuardianActor without AutoAcknowledge"

  val noAckGuardian = createGuardian(false)
  
  it should "pass on a message that appears on the channel" in {
    noAckGuardian ! someMessage

    expectMsg(AmqpRequestMessage(someMessageBody, someDeliveryTag))
  }
  
  it should "acknowledge a message, but only when requested to" in {
    noAckGuardian ! someMessage
    acknowledgeNever(someDeliveryTag)

    noAckGuardian ! Acknowledge(someDeliveryTag)
    acknowledgeOnce(someDeliveryTag)
  }
  
  it should "not acknowledge to the channel when the wrong delivery tag is acknowledged" in {
    val anotherDeliveryTag = someDeliveryTag + 1

    noAckGuardian ! someMessage
    noAckGuardian ! Acknowledge(anotherDeliveryTag)

    acknowledgeNever(someDeliveryTag)
    acknowledgeOnce(anotherDeliveryTag)
  }
  
  it should "explicitly Nack a message when it receives a terminate" in {
    noAckGuardian ! someMessage
    noAckGuardian ! Terminated
    
    verify (channel, times(1)).basicNack(someDeliveryTag, false, true)
  }
  
  
  
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
  
  private def acknowledgeNever(deliveryTag: Long) {
    verify (channel, never()).basicAck(deliveryTag, true)
    verify (channel, never()).basicAck(deliveryTag, false)
  }
  
  private def acknowledgeOnce(deliveryTag: Long) {
    verify (channel, times(1)).basicAck(deliveryTag, false)
    verify (channel, never()).basicAck(deliveryTag, true)
  }
  
  private def createGuardian(autoAcknowledge: Boolean): ActorRef = {
    val name = if (autoAcknowledge) "AutoAckTestGuardian" else "NoAckTestGuardian"
    val configuration = mock[QueueConfiguration]
    when (configuration.autoAcknowledge) thenReturn autoAcknowledge
    TestActorRef(new AmqpGuardianActor(testActor, channel, configuration))
  }
  
  private def createMessage(messageBody: String = someMessageBody, replyTo: String = someReplyTo, deliveryTag: Long = someDeliveryTag): AmqpRequestMessageWithProperties =
    AmqpRequestMessageWithProperties(someMessageBody, MessageProperties(replyTo = replyTo), someDeliveryTag)
}
