package com.nummulus.amqp.driver.test

import org.scalatest.FunSuite
import org.scalatest.Matchers
import org.scalatest.concurrent.Conductors
import com.nummulus.amqp.driver.AmqpDriver
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

@RunWith(classOf[JUnitRunner])
class ConnectionTest extends FunSuite with Conductors with Matchers {

  test("Configuring a consumer and provider, then sending a message") {
    val driver = AmqpDriver("ampq.conf")
    val consumer = driver.newConsumer("service.test", "Test")
    val provider = driver.newProvider("Test")
    
    val conductor = new Conductor
    import conductor._
    
    thread("consumer") {
      val response = consumer.ask("hello?")
      waitForBeat(2)
      val reaction = Await.result(response, 1.second)
      reaction should be ("world!")
      beat should be(2)
    }

    thread("producer") {
      waitForBeat(1)
      provider.bindCallBack( (_) => "world!")
      provider.handleNextDelivery()
      waitForBeat(2)
      beat should be(2)
    }
    
    whenFinished {
      beat should be(2)
    }
  }
}