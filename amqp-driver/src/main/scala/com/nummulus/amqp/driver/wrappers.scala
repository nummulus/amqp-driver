package com.nummulus.amqp.driver

import com.nummulus.amqp.driver.consumer.MessageConsumer
import com.rabbitmq.client.AMQP.Queue.DeclareOk
import com.rabbitmq.client.{Channel => RabbitChannel}
import com.rabbitmq.client.{Connection => RabbitConnection}
import com.rabbitmq.client.{ConnectionFactory => RabbitConnectionFactory}

/**
 * Wrapper classes around RabbitMQ client classes to ease testing.
 */
class ConnectionFactory(factory: RabbitConnectionFactory) {
  def setHost(host: String) {
    factory.setHost(host)
  }
  
  def newConnection(): Connection = new Connection(factory.newConnection())
}

class Connection(connection: RabbitConnection) {
  def createChannel(): Channel = new Channel(connection.createChannel)
  
  def close { connection.close() }
}

class Channel(channel: RabbitChannel) {
  def queueDeclare(): QueueDeclareOk = new QueueDeclareOk(channel.queueDeclare)
  
  def queueDeclare(queue: String, durable: Boolean, exclusive: Boolean, autoDelete: Boolean, arguments: Map[String, Object]): QueueDeclareOk = {
    import scala.collection.JavaConversions._
    
    new QueueDeclareOk(channel.queueDeclare(queue, durable, exclusive, autoDelete, arguments))
  }
  
  def basicConsume(queue: String, autoAcknowledge: Boolean, callback: MessageConsumer) {
    channel.basicConsume(queue, autoAcknowledge, callback.get)
  }
  
  private[driver] def get: RabbitChannel = channel
}

class QueueDeclareOk(declareOk: DeclareOk) {
  def getQueue: String = declareOk.getQueue
}