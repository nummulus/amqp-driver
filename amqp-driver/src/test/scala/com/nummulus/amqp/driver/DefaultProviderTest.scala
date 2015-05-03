package com.nummulus.amqp.driver

import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest._

import com.nummulus.amqp.driver.akka.AkkaMessageConsumer
import com.nummulus.amqp.driver.fixture.ProviderFixture
import com.nummulus.amqp.driver.matcher.TypeMatcher

import _root_.akka.actor.ActorSystem
import _root_.akka.testkit.TestKit

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class DefaultProviderTest extends TestKit(ActorSystem("test-system")) with FlatSpecLike with Matchers with TypeMatcher with BeforeAndAfterAll {
  behavior of "DefaultProvider"
  
  it should "bind to an actor created by a callback function" in new ProviderFixture("requestQueue0") {
    var factoryCalled = false
    val factory: AmqpProvider.ActorFactory = sender => {
      factoryCalled = true
      testActor
    }
    
    provider.bind(factory)
    
    factoryCalled should be (true)
  }
  
  def verifyMessageConsumption(channel: Channel): Unit = {
    val queueCaptor = ArgumentCaptor.forClass(classOf[String])
    val autoAcknowledgeCaptor = ArgumentCaptor.forClass(classOf[Boolean])
    val callbackCaptor = ArgumentCaptor.forClass(classOf[MessageConsumer])
    
    verify (channel).basicConsume(queueCaptor.capture(), autoAcknowledgeCaptor.capture(), anyString(), callbackCaptor.capture())
    
    queueCaptor.getValue should startWith ("requestQueue")
    autoAcknowledgeCaptor.getValue should be (true)
    callbackCaptor.getValue should be (ofType[AkkaMessageConsumer])
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }
}