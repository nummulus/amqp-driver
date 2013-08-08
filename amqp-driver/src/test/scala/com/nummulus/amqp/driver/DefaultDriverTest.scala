package com.nummulus.amqp.driver

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import com.typesafe.config.ConfigFactory

@RunWith(classOf[JUnitRunner])
class DefaultDriverTest extends FlatSpec with Matchers with MockitoSugar with OneInstancePerTest {
  behavior of "DefaultDriver"
  
  it should "connect to the broker after creating a consumer" in {
    verify (factory, never).newConnection
    
    driver.newConsumer("TestService", "testOperation")
    verify (factory, times(1)).newConnection
  }
  
  it should "set the broker host when creating a consumer" in {
    driver.newConsumer("TestService", "testOperation")
    verify (factory).setHost("localhost")
  }
  
  // Test fixture
  val factory = mock[ConnectionFactory]
  val connection = mock[Connection]
  val channel = mock[Channel]
  
  when (factory.newConnection) thenReturn connection
  when (connection.createChannel) thenReturn channel
  
  val driver = new DefaultDriver(factory, ConfigFactory.load("test"))
}
