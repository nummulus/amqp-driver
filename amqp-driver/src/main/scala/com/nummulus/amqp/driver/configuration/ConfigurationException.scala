package com.nummulus.amqp.driver.configuration

/**
 * Thrown if something went wrong while reading a configuration file.
 */
class ConfigurationException(message: String, cause: Throwable) extends Exception(message, cause)