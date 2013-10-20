package com.nummulus.amqp.driver

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import com.nummulus.amqp.driver.fixture.ConsumerFixture

@RunWith(classOf[JUnitRunner])
class DefaultConsumerTestForTell extends FlatSpec with Matchers with MockitoSugar {
  behavior of "DefaultConsumer#tell"
  
  it should "publish a message when calling tell" in new ConsumerFixture {
    consumer.tell("Cheese")
    
    verify (channel).basicPublish("", "requestQueue", null, "Cheese".getBytes)
  }
  
  it should "publish a message without expecting a response" in new ConsumerFixture {
    consumer.tell("Cheese")
    
    verify (messageConsumer, never()).nextDelivery
  }
}