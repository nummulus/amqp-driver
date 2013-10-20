package com.nummulus.amqp.driver.configuration

/**
 * Configuration of a queue.
 */
private[driver] case class QueueConfiguration(
    queue: String,
    durable: Boolean,
    exclusive: Boolean,
    autoDelete: Boolean,
    autoAcknowledge: Boolean)