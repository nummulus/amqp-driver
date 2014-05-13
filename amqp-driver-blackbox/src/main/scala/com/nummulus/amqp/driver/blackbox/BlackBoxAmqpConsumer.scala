package com.nummulus.amqp.driver.blackbox

import com.nummulus.amqp.driver.AmqpConsumer
import scala.concurrent.Future

class BlackBoxAmqpConsumer extends AmqpConsumer {
  /**
   * Sends a message asynchronously and returns a [[scala.concurrent.Future]]
   * holding the eventual response.
   */
  def ask(message: String): Future[String] = ???
  
  /**
   * Sends a message without waiting for a response, fire-and-forget semantics.
   */
  def tell(message: String): Unit = ???
}