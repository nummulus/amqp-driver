package com.nummulus.amqp.driver.blackbox

import org.junit.runner.RunWith
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.scalatest.Args
import org.scalatest.FlatSpec
import org.scalatest.FunSuite
import org.scalatest.Matchers
import org.scalatest.OneInstancePerTest
import org.scalatest.Reporter
import org.scalatest.Suite
import org.scalatest.mock.MockitoSugar

import akka.actor.ActorSystem

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class BlackBoxAmqpTestKitTest extends FlatSpec with Matchers with MockitoSugar with OneInstancePerTest {

  val actorSystem = mock[ActorSystem]
  val factory = mock[BlackBoxAmqpDriverFactory]
  val reporter = mock[Reporter]
  
  trait TheTestKit extends BlackBoxAmqpTestKit { this: Suite =>
    val system = actorSystem
    override lazy val amqpDriverFactory = factory
  }
  

  behavior of "BlackBoxAmqpTestKit"
  
  it should "call done() after a test is run" in {
    val suite = new FunSuite with TheTestKit {
      test("this is a test") {}
    }

    suite.run(None, Args(reporter))
    verify(factory).done()
  }
  
  it should "call done() for every test in the suite" in {
    val suite = new FunSuite with TheTestKit {
      test("this is the 1st test") {}
      test("this is the 2nd test") {}
      test("this is the 3rd test") {}
    }

    suite.run(None, Args(reporter))
    verify(factory, times(3)).done()
  }
}
