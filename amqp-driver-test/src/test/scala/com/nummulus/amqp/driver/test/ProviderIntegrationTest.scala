package com.nummulus.amqp.driver.test

import org.mockito.Mockito._
import org.scalatest._

import com.nummulus.amqp.driver.api.provider.AmqpProviderRequest
import com.nummulus.amqp.driver.fixture.BoundProviderFixture

class ProviderIntegrationTest extends FlatSpec with Matchers {
  behavior of "AmqpProvider"
  
  val SomeDeliveryTag = 1;
  
  it should "forward a message to the provided actor" in new BoundProviderFixture(true) {
    sendMessage(SomeDeliveryTag, "Hi")
    
    probe.expectMsg(AmqpProviderRequest("Hi", SomeDeliveryTag))
  }
}