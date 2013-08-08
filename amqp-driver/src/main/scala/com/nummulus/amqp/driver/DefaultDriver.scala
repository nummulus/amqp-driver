package com.nummulus.amqp.driver

import org.slf4j.LoggerFactory
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

/**
 * Default driver implementation.
 * 
 * Every driver has a single connection with the broker. The connection is only
 * established if a consumer is created. Every consumer will get a separate
 * channel.
 */
private[driver] class DefaultDriver(connectionFactory: ConnectionFactory, config: Config) extends AmqpDriver {
  private val logger = LoggerFactory.getLogger(getClass)
  private val rootConfig = config.getConfig("amqp")
  
  private lazy val connection = createConnection()
  
  /**
   * Creates a new consumer for the specified service operation.
   * 
   * If no connection to the broker is available, one will be established.
   * 
   * @param service owner of the operation
   * @param operation operation name of the operation to invoke
   * @return new consumer
   */
  override def newConsumer(service: String, operation: String): AmqpConsumer = {
    val channel = connection.createChannel()
    null
  }
  
  /**
   * Returns a newly created connection to the broker.
   */
  private def createConnection(): Connection = {
    val host = rootConfig.getString("host")
    
    logger.info("Connecting to AMQP broker at {}", host)
    connectionFactory.setHost(host)
    connectionFactory.newConnection()
  }
}