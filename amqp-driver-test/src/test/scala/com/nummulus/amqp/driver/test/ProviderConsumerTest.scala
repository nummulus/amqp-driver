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

@RunWith(classOf[JUnitRunner])
class ConnectionTest extends FlatSpec with Matchers with ScalaFutures with TableDrivenPropertyChecks {

  val propertyFiles = Table(("file"),
    ("ProviderConsumer_1.conf"),
    ("ProviderConsumer_2.conf"),
    ("ProviderConsumer_3.conf"),
    ("ProviderConsumer_4.conf")
  )

  forAll(propertyFiles) { (propertyFile : String) =>
    behavior of "The Provider/Consumer System  with configuration " + propertyFile
    
    it should "deliver a message back to the consumer" in new ProviderConsumerFixture(propertyFile) {
      val system = ActorSystem("Test")
      val actor = system.actorOf(Props[EchoActor])
      provider.bind(actor)

      val response = consumer.ask("hello?")
      whenReady(response) { s =>
        s should be("world!")
      }
      provider.unbind();
    }
  }
  
  forAll(propertyFiles) { (propertyFile : String) =>
    behavior of "The Consumer with configuration " + propertyFile
    
    it should "send a message to the provider" in new ProviderConsumerFixture(propertyFile) {
      implicit val system = ActorSystem("Test")
      val probe = TestProbe()
      provider.bind(probe.ref)

      val response = consumer.ask("hello?")
      probe.expectMsg(AmqpRequestMessage("hello?", 1))
      provider.unbind();
    }
  }
}

class EchoActor extends Actor {
  def receive = {
    case AmqpRequestMessage(body, deliveryTag) => {
      sender ! AmqpResponseMessage("world!", deliveryTag)
    }
  }
}