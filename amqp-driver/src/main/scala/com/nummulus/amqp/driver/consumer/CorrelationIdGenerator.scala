package com.nummulus.amqp.driver.consumer

/**
 * Generator for correlation id's, used to identify which response on a queue
 * belongs to which request.
 */
trait CorrelationIdGenerator {
  /**
   * Returns a unique, generated correlation id.
   */
  def generate: String
}