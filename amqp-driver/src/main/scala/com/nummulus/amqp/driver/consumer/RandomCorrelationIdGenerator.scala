package com.nummulus.amqp.driver.consumer

import java.util.UUID

/**
 * Generates correlation id's based on UUIDs.
 */
class RandomCorrelationIdGenerator extends CorrelationIdGenerator {
  /**
   * Returns a new UUID.
   */
  override def generate: String = UUID.randomUUID.toString
}