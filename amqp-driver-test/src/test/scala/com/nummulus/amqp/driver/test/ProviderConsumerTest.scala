package com.nummulus.amqp.driver.test

import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit._
import org.scalatest.prop.TableDrivenPropertyChecks

import com.nummulus.amqp.driver.api.consumer.AmqpConsumerRequest
import com.nummulus.amqp.driver.api.consumer.AmqpConsumerResponse
import com.nummulus.amqp.driver.api.provider.AmqpProviderRequest
import com.nummulus.amqp.driver.api.provider.AmqpProviderResponse
import com.nummulus.amqp.driver.fixture.ProviderConsumerFixture

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.ImplicitSender
import akka.testkit.TestKit

@RunWith(classOf[JUnitRunner])
class ProviderConsumerTest extends TestKit(ActorSystem("test-system")) with ImplicitSender with FlatSpecLike with Matchers 
  with ScalaFutures with TableDrivenPropertyChecks with BeforeAndAfterAll {
  
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val propertyFiles = Table(("file"),
    ("ProviderConsumer_1.conf"),
    ("ProviderConsumer_2.conf"),
    ("ProviderConsumer_3.conf"),
    ("ProviderConsumer_4.conf")
  )

  forAll(propertyFiles) { (propertyFile : String) =>
    behavior of "The Provider/Consumer System with configuration " + propertyFile
    
    it should "deliver a message back to the consumer" in new ProviderConsumerFixture(propertyFile) {
      provider.bind(system.actorOf(Props[EchoActor]))

      consumer ! AmqpConsumerRequest("hello?", Some(self))
      
      expectMsg(AmqpConsumerResponse("hello?"))
      
      provider.unbind();
    }
  }
  
  forAll(propertyFiles) { (propertyFile : String) =>
    behavior of "The Consumer with configuration " + propertyFile
    
    it should "ask a provider a question that arrives" in new ProviderConsumerFixture(propertyFile) {
      provider.bind(testActor)

      consumer ! AmqpConsumerRequest("hello?", Some(self))
      
      expectMsg(AmqpProviderRequest("hello?", 1))
      
      provider.unbind();
    }
  }
  
  forAll(propertyFiles) { (propertyFile : String) =>
    behavior of "The Consumer with configuration " + propertyFile
    
    it should "tell a provider a message that arrives" in new ProviderConsumerFixture(propertyFile) {
      provider.bind(testActor)

      consumer ! AmqpConsumerRequest("I pity the fool!")
      
      expectMsg(AmqpProviderRequest("I pity the fool!", 1))
      
      provider.unbind();
    }
  }
}

private class EchoActor extends Actor {
  def receive = {
    case AmqpProviderRequest(body, deliveryTag) => {
      sender ! AmqpProviderResponse(body, deliveryTag)
    }
  }
}