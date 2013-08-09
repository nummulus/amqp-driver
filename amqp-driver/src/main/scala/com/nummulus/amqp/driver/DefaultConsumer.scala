package com.nummulus.amqp.driver

import com.nummulus.amqp.driver.configuration.QueueConfiguration

/**
 * Default consumer implementation.
 */
private[driver] class DefaultConsumer(channel: Channel, config: QueueConfiguration) extends AmqpConsumer {}