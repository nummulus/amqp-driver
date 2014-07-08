package com.nummulus.amqp.driver

import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest._

import com.nummulus.amqp.driver.fixture.ProviderFixture
import com.nummulus.amqp.driver.matcher.TypeMatcher
import com.nummulus.amqp.driver.provider.AkkaMessageConsumer

import _root_.akka.actor.ActorSystem
import _root_.akka.testkit.TestKit

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class DefaultProviderTest extends TestKit(ActorSystem("test-system")) with FlatSpecLike with Matchers with TypeMatcher with BeforeAndAfterAll {
  behavior of "DefaultProvider"
  
  it should "declare a request queue at construction time" in new ProviderFixture {
    verify (channel).queueDeclare("requestQueue", true, false, false, null)
  }
  
  it should "set the QOS to one" in new ProviderFixture {
    verify (channel).basicQos(1)
  }
  
  it should "consume messages from the queue after calling bind" in new ProviderFixture {
    provider.bind(testActor)
    
    verifyMessageConsumption(channel)
  }
  
  it should "bind to an actor created by a callback function" in new ProviderFixture {
    var factoryCalled = false
    val factory: AmqpProvider.ActorFactory = sender => {
      factoryCalled = true
      testActor
    }
    
    provider.bind(factory)
    
    factoryCalled should be (true)
    verifyMessageConsumption(channel)
  }
  
  it should "not receive messages after an unbind" in new ProviderFixture {
    provider.bind(testActor)
    provider.unbind()
    
    verify (channel).basicCancel(anyString)
  }
  
  it should "fail when an attempt to re-bind after unbind is made" in new ProviderFixture {
    provider.bind(testActor)
    provider.unbind()
    
    intercept[IllegalStateException] {
      provider.bind(testActor)
    }
  }
  
  def verifyMessageConsumption(channel: Channel): Unit = {
    val queueCaptor = ArgumentCaptor.forClass(classOf[String])
    val autoAcknowledgeCaptor = ArgumentCaptor.forClass(classOf[Boolean])
    val callbackCaptor = ArgumentCaptor.forClass(classOf[MessageConsumer])
    
    verify (channel).basicConsume(queueCaptor.capture(), autoAcknowledgeCaptor.capture(), anyString(), callbackCaptor.capture())
    
    queueCaptor.getValue should be ("requestQueue")
    autoAcknowledgeCaptor.getValue should be (true)
    callbackCaptor.getValue should be (ofType[AkkaMessageConsumer])
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }
}