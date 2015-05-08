package com.nummulus.amqp.driver.fixture

import com.nummulus.amqp.driver.ConnectionFactory
import com.nummulus.amqp.driver.DefaultDriver
import com.rabbitmq.client.{ ConnectionFactory => RabbitConnectionFactory }
import com.typesafe.config._
import com.typesafe.config.ConfigFactory

class ProviderConsumerFixture(fileName: String) {
  val config = ConfigFactory.load(fileName)
  val connectionFactory = new ConnectionFactory(new RabbitConnectionFactory)
  val driver = new DefaultDriver(connectionFactory, config)
  val consumer = driver.newConsumer("service.test", "Test")
  val provider = driver.newProvider("Test")
}