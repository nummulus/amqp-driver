package com.nummulus.amqp.driver

import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.junit._

import com.nummulus.amqp.driver.fixture.ProviderFixture
import com.nummulus.amqp.driver.matcher.TypeMatcher
import com.nummulus.amqp.driver.provider.AkkaMessageConsumer

import _root_.akka.actor.ActorSystem
import _root_.akka.testkit.TestProbe

@RunWith(classOf[JUnitRunner])
class DefaultProviderTest extends FlatSpec with Matchers with TypeMatcher {
  behavior of "DefaultProvider"
  
  it should "declare a request queue at construction time" in new ProviderFixture {
    verify (channel).queueDeclare("requestQueue", true, false, false, null)
  }
  
  it should "set the QOS to one" in new ProviderFixture {
    verify (channel).basicQos(1)
  }
  
  it should "consume messages from the queue after calling bind" in new ProviderFixture {
    implicit val system = ActorSystem("Test")
    val probe = TestProbe()
    
    provider.bind(probe.ref)
    
    val queueCaptor = ArgumentCaptor.forClass(classOf[String])
    val autoAcknowledgeCaptor = ArgumentCaptor.forClass(classOf[Boolean])
    val callbackCaptor = ArgumentCaptor.forClass(classOf[MessageConsumer])
    
    verify (channel).basicConsume(queueCaptor.capture(), autoAcknowledgeCaptor.capture(), callbackCaptor.capture())
    
    queueCaptor.getValue should be ("requestQueue")
    autoAcknowledgeCaptor.getValue should be (true)
    callbackCaptor.getValue should be (ofType[AkkaMessageConsumer])
  }
}