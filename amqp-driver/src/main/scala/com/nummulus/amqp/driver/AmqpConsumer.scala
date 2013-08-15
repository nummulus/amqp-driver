package com.nummulus.amqp.driver

import scala.concurrent.Future

/**
 * A consumer of a service, which in turn is offered by a provider.
 */
trait AmqpConsumer {
  /**
   * Sends a message asynchronously and returns a [[scala.concurrent.Future]]
   * holding the eventual response.
   */
  def ask(message: String): Future[String]
}