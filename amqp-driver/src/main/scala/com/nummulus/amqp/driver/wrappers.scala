package com.nummulus.amqp.driver

import scala.collection.JavaConversions

import com.rabbitmq.client.AMQP.BasicProperties

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
  
  def close() { connection.close() }
}

class Channel(channel: RabbitChannel) {
  def queueDeclare(): QueueDeclareOk = new QueueDeclareOk(channel.queueDeclare)
  
  def queueDeclare(queue: String, durable: Boolean, exclusive: Boolean, autoDelete: Boolean, arguments: Map[String, Object]): QueueDeclareOk = {
    val queueArgs = if (arguments == null) null else JavaConversions.mapAsJavaMap(arguments)
    new QueueDeclareOk(channel.queueDeclare(queue, durable, exclusive, autoDelete, queueArgs))
  }
  
  def basicConsume(queue: String, autoAcknowledge: Boolean, callback: MessageConsumer) {
    channel.basicConsume(queue, autoAcknowledge, callback.get)
  }
  
  def basicPublish(exchange: String, routingKey: String, properties: MessageProperties, body: Array[Byte]) {
    import scala.collection.JavaConversions._
    
    val props = if (properties == null) {
      null 
    } else {
      new BasicProperties().builder
        .contentType(properties.contentType)
        .contentEncoding(properties.contentEncoding)
        .headers(properties.headers)
        .deliveryMode(properties.deliveryMode)
        .priority(properties.priority)
        .correlationId(properties.correlationId)
        .replyTo(properties.replyTo)
        .expiration(properties.expiration)
        .messageId(properties.messageId)
        .userId(properties.userId)
        .appId(properties.appId)
        .clusterId(properties.cluserId)
        .build
    }
    
    channel.basicPublish(exchange, routingKey, props, body)
  }
  
  def basicAck(deliveryTag: Long, multiple: Boolean) {
    channel.basicAck(deliveryTag, multiple)
  }
  
  def basicNack(deliveryTag: Long, multiple: Boolean, requeue: Boolean) {
    channel.basicNack(deliveryTag, multiple, requeue)
  }
  
  def basicQos(prefetchCount: Int) {
    channel.basicQos(prefetchCount)
  }
  
  private[driver] def get: RabbitChannel = channel
}

class QueueDeclareOk(declareOk: DeclareOk) {
  def getQueue: String = declareOk.getQueue
}

case class MessageProperties(
    contentType: String = "application/json",
    contentEncoding: String = null,
    headers: Map[String, AnyRef] = Map(),
    deliveryMode: Int = 1,
    priority: Int = 0,
    correlationId: String = null,
    replyTo: String = null,
    expiration: String = null,
    messageId: String = null,
    userId: String = null,
    appId: String = null,
    cluserId: String = null)
