package com.nummulus.amqp.driver.test

import org.scalatest.FunSuite
import org.scalatest.Matchers
import org.scalatest.concurrent.Conductors
import com.nummulus.amqp.driver.AmqpDriver
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.TestProbe
import com.nummulus.amqp.driver.akka.AmqpRequestMessage
import com.nummulus.amqp.driver.fixture.ProviderConsumerFixture
import org.scalatest.FlatSpec
import org.scalatest.concurrent.Futures
import org.scalatest.concurrent.ScalaFutures
import com.nummulus.amqp.driver.akka.AmqpRequestMessage
import com.nummulus.amqp.driver.akka.AmqpResponseMessage
import org.scalatest.prop.PropertyChecks
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.OneInstancePerTest
import com.nummulus.amqp.driver.akka.Acknowledge
import org.scalatest.FlatSpecLike
import akka.testkit.TestKit

@RunWith(classOf[JUnitRunner])
class ProviderConsumerTest extends TestKit(ActorSystem("test-system")) with FlatSpecLike with Matchers 
  with ScalaFutures with TableDrivenPropertyChecks {

  val propertyFiles = Table(("file"),
    ("ProviderConsumer_1.conf"),
    ("ProviderConsumer_2.conf"),
    ("ProviderConsumer_3.conf"),
    ("ProviderConsumer_4.conf")
  )

  forAll(propertyFiles) { (propertyFile : String) =>
    behavior of "The Provider/Consumer System  with configuration " + propertyFile
    
    it should "deliver a message back to the consumer" in new ProviderConsumerFixture(propertyFile) {
      provider.bind(system.actorOf(Props[EchoActor]))

      val response = consumer.ask("hello?")
      whenReady(response) { s =>
        s should be("hello?")
      }
      
      provider.unbind();
    }
  }
  
  forAll(propertyFiles) { (propertyFile : String) =>
    behavior of "The Consumer with configuration " + propertyFile
    
    it should "asks a provider a question and checks the result" in new ProviderConsumerFixture(propertyFile) {
      provider.bind(testActor)

      consumer.ask("hello?")
      expectMsg(AmqpRequestMessage("hello?", 1))
      
      provider.unbind();
    }
  }
  
  forAll(propertyFiles) { (propertyFile : String) =>
    behavior of "The Consumer with configuration " + propertyFile
    
    it should "tells a provider" in new ProviderConsumerFixture(propertyFile) {
      provider.bind(testActor)

      consumer.tell("I pity the fool!")
      expectMsg(AmqpRequestMessage("I pity the fool!", 1))
      
      provider.unbind();
    }
  }
}

private class EchoActor extends Actor {
  def receive = {
    case AmqpRequestMessage(body, deliveryTag) => {
      sender ! AmqpResponseMessage(body, deliveryTag)
    }
  }
}