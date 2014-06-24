package com.nummulus.amqp.driver

import java.util.UUID

/**
 * Contains generators that can be used to generate ids, for example:
 * - consumer tags to uniquely identify a queue
 * - correlation ids to identify which response on a queue belongs to which request
 */
object IdGenerators {
  type IdGenerator = () => String

  /**
   * Generator for random ids based on UUID
   */
  val random: IdGenerator = () => UUID.randomUUID.toString
}