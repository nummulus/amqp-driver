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

@RunWith(classOf[JUnitRunner])
class ConnectionTest extends FlatSpec with Matchers with ScalaFutures {
  behavior of "Provider/Consumer System"

  ignore should "Check if a message from a consumer is delivered at an actor bound to a provider" in new ProviderConsumerFixture("ProviderConsumer.conf") {
    implicit val system = ActorSystem("Test")
    val probe = TestProbe()
    provider.bind(probe.ref)

    val response = consumer.ask("hello?")
    probe.expectMsg(AmqpRequestMessage("hello?", 1))
    provider.unbind();
  }

  it should "Deliver a message back to the consumer" in new ProviderConsumerFixture("ProviderConsumer.conf") {
    val system = ActorSystem("Test")
    val actor = system.actorOf(Props[EchoActor])
    provider.bind(actor)

    val response = consumer.ask("hello?")
    whenReady(response) { s =>
      s should be("world!")
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