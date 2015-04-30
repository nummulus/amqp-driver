package com.nummulus.amqp.driver.test

import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.junit._

import com.nummulus.amqp.driver.api.provider.AmqpRequestMessage
import com.nummulus.amqp.driver.fixture.BoundProviderFixture

@RunWith(classOf[JUnitRunner])
class ProviderIntegrationTest extends FlatSpec with Matchers {
  behavior of "AmqpProvider"
  
  val SomeDeliveryTag = 1;
  
  it should "forward a message to the provided actor" in new BoundProviderFixture(true) {
    sendMessage(SomeDeliveryTag, "Hi")
    
    probe.expectMsg(AmqpRequestMessage("Hi", SomeDeliveryTag))
  }
}