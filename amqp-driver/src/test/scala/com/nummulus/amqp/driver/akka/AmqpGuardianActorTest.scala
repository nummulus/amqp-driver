package com.nummulus.amqp.driver.akka

import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import com.nummulus.amqp.driver.Channel
import com.nummulus.amqp.driver.MessageProperties
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.actor.Props
import org.scalatest.OneInstancePerTest
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock

@RunWith(classOf[JUnitRunner])
class AmqpGuardianActorTest extends TestKit(ActorSystem("test-system")) with FlatSpecLike with Matchers
    with MockitoSugar with BeforeAndAfterAll with OneInstancePerTest {
  
  val channel = mock[Channel]
  var ackCount = 0

  val someMessage = "some message"
  val someReplyTo = "some Rabbit channel"
  val someDeliveryTag = 42L
  
  
  
  behavior of "AmqpGuardianActor with AutoAcknowledge"

  val autoAckGuardian = createGuardian(true)
  
  it should "pass on a message that appears on the channel and acknowledge it automatically" in {
    val msg = createMessage(someReplyTo)
    
    autoAckGuardian ! msg
    expectMsg(AmqpRequestMessage(someMessage, someDeliveryTag))
    verify (channel, times(1)).basicAck(anyLong, anyBoolean)

    reset(channel)

    autoAckGuardian ! Acknowledge(someDeliveryTag)
    verify (channel, never()).basicAck(anyLong, anyBoolean)
  }
  
  
  
  behavior of "AmqpGuardianActor without AutoAcknowledge"

  val noAckGuardian = createGuardian(false)
  
  it should "pass on a message that appears on the channel and acknowledge it when requested to" in {
    val msg = createMessage(someReplyTo)
    
    noAckGuardian ! msg
    
    expectMsg(AmqpRequestMessage(someMessage, someDeliveryTag))
    verify (channel, never()).basicAck(anyLong, anyBoolean)

    reset(channel)

    noAckGuardian ! Acknowledge(someDeliveryTag)
    Thread.sleep(1000)
    verify (channel, times(1)).basicAck(anyLong, anyBoolean)
  }
  
  
  
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  private def createGuardian(autoAcknowledge: Boolean): ActorRef = {
    val name = if (autoAcknowledge) "AutoAckTestGuardian" else "NoAckTestGuardian"
    val configuration = mock[QueueConfiguration]
    when (configuration.autoAcknowledge) thenReturn autoAcknowledge
    system.actorOf(Props(classOf[AmqpGuardianActor], testActor, channel, configuration), name)
  }
  
  private def createMessage(replyTo: String): AmqpRequestMessageWithProperties =
    AmqpRequestMessageWithProperties(someMessage, MessageProperties(replyTo = replyTo), someDeliveryTag)
}
