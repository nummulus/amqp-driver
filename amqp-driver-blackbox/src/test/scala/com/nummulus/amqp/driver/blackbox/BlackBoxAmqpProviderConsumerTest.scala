package com.nummulus.amqp.driver.blackbox

import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers
import org.scalatest.OneInstancePerTest
import org.scalatest.concurrent.ScalaFutures

import com.nummulus.amqp.driver.akka.AmqpRequestMessage
import com.nummulus.amqp.driver.akka.AmqpResponseMessage

import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.testkit.TestProbe

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class BlackBoxAmqpProviderConsumerTest extends TestKit(ActorSystem("test-system"))
    with FlatSpecLike with Matchers with OneInstancePerTest with BeforeAndAfterAll with ScalaFutures {
  
  val SomeMessage = "some message"
  val SomeAnswer = "some answer"
  val probe = TestProbe()

  val pc = new BlackBoxAmqpProviderConsumer(system)
  
  
  behavior of "tell"
  
  it should "throw when no actor is bound" in {
    an [IllegalStateException] should be thrownBy pc.tell(SomeMessage)
  }
  
  it should "forward a message" in {
    pc.bind(probe.ref)
    pc.tell(SomeMessage)

    probe.expectMsgPF() {
      case AmqpRequestMessage(SomeMessage, _) => true
    }
  }

  
  behavior of "ask"
  
  it should "throw when no actor is bound" in {
    an [IllegalStateException] should be thrownBy pc.ask(SomeMessage)
  }
  
  it should "forward a message and return an answer" in {
    pc.bind(probe.ref)
    
    val f = pc.ask(SomeMessage)
    
    probe.expectMsgPF() {
      case AmqpRequestMessage(SomeMessage, tag) =>
        probe.sender ! AmqpResponseMessage(SomeAnswer, tag)
    }
    
    whenReady(f) { _ should be (SomeAnswer) }
  }
  
  ignore should "crash when two replies are sent" in {
    pc.bind(probe.ref)
    
    pc.ask(SomeMessage)
    
    probe.expectMsgPF() {
      case AmqpRequestMessage(SomeMessage, tag) =>
        probe.sender ! AmqpResponseMessage(SomeAnswer, tag)
        probe.sender ! AmqpResponseMessage(SomeAnswer, tag)
    }
    
    // assert that the driver has shut down
  }
  
  
  behavior of "bind"
  
  it should "throw when a second actor is bound" in {
    pc.bind(probe.ref)
    an [IllegalStateException] should be thrownBy pc.bind(probe.ref)
  }
  
  
  behavior of "unbind"
  
  it should "throw when no actor is bound" in {
    an [IllegalStateException] should be thrownBy pc.unbind()
  }
  
  it should "make the BlackBoxProviderConsumer unusable for tell" in {
    pc.bind(probe.ref)
    pc.unbind()

    an [IllegalStateException] should be thrownBy pc.tell(SomeMessage)
  }
  
  it should "make the BlackBoxProviderConsumer unusable for ask" in {
    pc.bind(probe.ref)
    pc.unbind()

    an [IllegalStateException] should be thrownBy pc.ask(SomeMessage)
  }


  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }
}