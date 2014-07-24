package com.nummulus.amqp.driver.blackbox

import akka.actor.ActorSystem
import org.scalatest.BeforeAndAfter
import org.scalatest.Suite

/**
 * Mix in this trait to test an application that uses [[AmqpDriver]].
 * It makes automatically sure that the BlackBoxAmqpDriver is terminated correctly.
 * 
 * Note: ScalaTest's Spec or SpecLike must be mixed in *before* this one.
 * Also, you must implement [[system]], unless your test also extends Akka's
 * [[TestKit]]
 */
trait BlackBoxAmqpTestKit extends BeforeAndAfter { this: Suite =>

  /**
   * Implement this if you don't extend from Akka's [[TestKit]].
   */
  def system: ActorSystem

  lazy val amqpDriverFactory = new BlackBoxAmqpDriverFactory(system)

  after {
    amqpDriverFactory.done()
  }
}