package com.nummulus.amqp.driver

import com.rabbitmq.client.{Channel => RabbitChannel}
import com.rabbitmq.client.{Connection => RabbitConnection}
import com.rabbitmq.client.{ConnectionFactory => RabbitConnectionFactory}

/**
 * Wrapper classes around RabbitMQ client classes to ease testing.
 */
private[driver] class ConnectionFactory(factory: RabbitConnectionFactory) {
  def setHost(host: String) {
    factory.setHost(host)
  }
  
  def newConnection(): Connection = new Connection(factory.newConnection())
}

private[driver] class Connection(connection: RabbitConnection) {
  def createChannel(): Channel = new Channel(connection.createChannel)
  
  def close { connection.close() }
}

private[driver] class Channel(channel: RabbitChannel)