package com.nummulus.amqp.driver

import scala.collection.JavaConversions
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.AMQP.Queue.DeclareOk
import com.rabbitmq.client.{Channel => RabbitChannel}
import com.rabbitmq.client.{Connection => RabbitConnection}
import com.rabbitmq.client.{ConnectionFactory => RabbitConnectionFactory}
import com.rabbitmq.client.ShutdownListener
import com.rabbitmq.client.ShutdownSignalException
import org.slf4j.LoggerFactory

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
  channel.addShutdownListener(ShutdownLogger())
  
  def queueDeclare(): QueueDeclareOk = new QueueDeclareOk(channel.queueDeclare)
  
  def queueDeclare(queue: String, durable: Boolean, exclusive: Boolean, autoDelete: Boolean, arguments: Map[String, Object]): QueueDeclareOk = {
    val queueArgs = if (arguments == null) null else JavaConversions.mapAsJavaMap(arguments)
    new QueueDeclareOk(channel.queueDeclare(queue, durable, exclusive, autoDelete, queueArgs))
  }
  
  def basicConsume(queue: String, autoAcknowledge: Boolean, consumerTag: String, callback: MessageConsumer): String =
    channel.basicConsume(queue, autoAcknowledge, consumerTag, callback.get)
  
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
        .clusterId(properties.clusterId)
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
  
  def basicCancel(consumerTag: String) {
    channel.basicCancel(consumerTag)
  }
  
  private[driver] def get: RabbitChannel = channel
}

class QueueDeclareOk(declareOk: DeclareOk) {
  def getQueue: String = declareOk.getQueue
}

class ShutdownLogger extends ShutdownListener {
  private val logger = LoggerFactory.getLogger(getClass)
  def shutdownCompleted(cause: ShutdownSignalException): Unit = logger.info("Closed channel, because: {}", cause.getReason())
}

object ShutdownLogger {
  def apply(): ShutdownLogger = new ShutdownLogger()
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
    clusterId: String = null)

object MessageProperties {
  def apply(props: AMQP.BasicProperties): MessageProperties =
    MessageProperties(
        contentType = props.getContentType(),
        contentEncoding = props.getContentEncoding(),
        headers = null,
        deliveryMode = integer2int(props.getDeliveryMode, 1),
        priority = integer2int(props.getPriority(), 0),
        correlationId = props.getCorrelationId(),
        replyTo = props.getReplyTo(),
        expiration = props.getExpiration(),
        messageId = props.getMessageId(),
        userId = props.getUserId(),
        appId = props.getAppId(),
        clusterId = props.getClusterId())
  
  /**
   * Returns the value if it's not null, returns the default value otherwise.
   */
  private def integer2int(value: Integer, defaultValue: Int): Int = if (value != null) value else defaultValue
}
