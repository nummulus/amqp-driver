package com.nummulus.amqp.driver

import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.junit._
import com.nummulus.amqp.driver.fixture.ProviderFixture

@RunWith(classOf[JUnitRunner])
class DefaultProviderTest extends FlatSpec with Matchers {
  behavior of "DefaultProvider"
  
  it should "declare a request queue at construction time" in new ProviderFixture {
    verify (channel).queueDeclare("requestQueue", true, false, false, null)
  }
}
