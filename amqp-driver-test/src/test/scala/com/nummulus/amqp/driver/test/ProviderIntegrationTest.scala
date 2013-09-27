package com.nummulus.amqp.driver.test

import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.junit._

import com.nummulus.amqp.driver.akka.AmqpRequestMessage
import com.nummulus.amqp.driver.fixture.BoundProviderFixture
import com.nummulus.amqp.driver.fixture.ProviderFixture
import com.nummulus.amqp.driver.provider.AkkaMessageConsumer
import com.rabbitmq.client.Envelope
import com.rabbitmq.client.AMQP.BasicProperties

import _root_.akka.actor.Actor
import _root_.akka.actor.ActorSystem
import _root_.akka.actor.Identify
import _root_.akka.testkit.ImplicitSender
import _root_.akka.testkit.TestActorRef
import _root_.akka.testkit.TestKit
import _root_.akka.testkit.TestProbe

@RunWith(classOf[JUnitRunner])
class ProviderIntegrationTest extends FlatSpec with Matchers {
  behavior of "AmqpProvider"
  
  it should "forward a message to the provided actor" in new BoundProviderFixture(true) {
    sendMessage("Hi")
    
    probe.expectMsg(AmqpRequestMessage("Hi", 1))
  }
  
  it should "acknowledge messages if auto acknowledge is enabled" in new BoundProviderFixture(true) {
    sendMessage("Hi")
    
    Thread.sleep(500)
    
    verify (channel).basicAck(1, false)
  }
}