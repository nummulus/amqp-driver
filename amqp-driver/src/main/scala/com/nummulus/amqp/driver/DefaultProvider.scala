package com.nummulus.amqp.driver

import org.slf4j.LoggerFactory
import com.nummulus.amqp.driver.configuration.QueueConfiguration

/**
 * Default provider implementation.
 */
private[driver] class DefaultProvider(channel: Channel, configuration: QueueConfiguration) extends AmqpProvider {
  private val logger = LoggerFactory.getLogger(getClass)
}