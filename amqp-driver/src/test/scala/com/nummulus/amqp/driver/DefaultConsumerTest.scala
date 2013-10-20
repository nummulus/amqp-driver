package com.nummulus.amqp.driver

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit._
import org.mockito.Mockito._
import com.nummulus.amqp.driver.fixture.ConsumerFixture

@RunWith(classOf[JUnitRunner])
class DefaultConsumerTest extends FlatSpec with Matchers {
  behavior of "DefaultConsumer"
  
  it should "declare a response queue at construction time" in new ConsumerFixture {
    verify (channel).queueDeclare
  }
  
  it should "declare a request queue at construction time" in new ConsumerFixture {
    verify (channel).queueDeclare("requestQueue", true, false, false, null)
  }
  
  it should "tie a consumer to the response queue at construction time" in new ConsumerFixture {
    verify (channel).basicConsume("generated-queue-name", true, messageConsumer)
  }
}
