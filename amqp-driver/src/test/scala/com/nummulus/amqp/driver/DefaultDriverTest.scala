package com.nummulus.amqp.driver

import org.scalatest._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigException
import com.nummulus.amqp.driver.configuration.ConfigurationException

class DefaultDriverTest extends FlatSpec with Matchers with MockitoSugar with OneInstancePerTest {
  behavior of "DefaultDriver"
  
  it should "connect to the broker after creating a consumer actor" in {
    verify (factory, never).newConnection
    
    driver.newConsumer("TestService", "testOperation")
    verify (factory, times(1)).newConnection
  }
  
  it should "set the broker host when creating a consumer actor" in {
    driver.newConsumer("TestService", "testOperation")
    verify (factory).setHost("localhost")
  }
  
  it should "create a channel when creating a consumer actor" in {
    driver.newConsumer("TestService", "testOperation")
    verify (connection).createChannel
  }
  
  it should "throw an exception if the service doesn't exist when creating a consumer actor" in {
    val exception = intercept[ConfigurationException] {
      driver.newConsumer("NonExistingService", "testOperation")
    }
    
    exception.getMessage should be ("No configuration setting found for key 'uses.NonExistingService'")
  }
  
  it should "throw an exception if the operation doesn't exist for a valid service when creating a consumer actor" in {
    val thrown = intercept[ConfigurationException] {
      driver.newConsumer("TestService", "nonExistingTestOperation")
    }
    
    thrown.getMessage should be ("No configuration setting found for key 'nonExistingTestOperation'")
  }
  
  
  it should "connect to the broker after creating a provider" in {
    verify (factory, never).newConnection
    
    driver.newProvider("parseFile")
    verify (factory, times(1)).newConnection
  }
  
  it should "set the broker host when creating a provider" in {
    driver.newProvider("parseFile")
    verify (factory).setHost("localhost")
  }
  
  it should "create a channel when creating a provider" in {
    driver.newProvider("parseFile")
    verify (connection).createChannel
  }
  
  it should "throw an exception if the operation doesn't exist when creating a provider" in {
    val exception = intercept[ConfigurationException] {
      driver.newProvider("nonExistingOperation")
    }
    
    exception.getMessage should be ("No configuration setting found for key 'nonExistingOperation'")
  }
  
  // Test fixture
  val factory = mock[ConnectionFactory]
  val connection = mock[Connection]
  val channel = mock[Channel]
  val declareOk = mock[QueueDeclareOk]
  
  when (factory.newConnection) thenReturn connection
  when (connection.createChannel) thenReturn channel
  when (channel.queueDeclare) thenReturn declareOk
  when (channel.queueDeclare("TestService.TestOperation", true, false, false, null)) thenReturn declareOk
  when (channel.queueDeclare("Importeur.ParseFile", true, false, false, null)) thenReturn declareOk
  when (declareOk.getQueue) thenReturn "generated-queue-name"
  
  val driver = new DefaultDriver(factory, ConfigFactory.load("test"))
}
