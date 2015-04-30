package com.nummulus.amqp.driver.fixture

import com.nummulus.amqp.driver.AmqpDriver
import com.nummulus.amqp.driver.DefaultDriver
import com.nummulus.amqp.driver.ConnectionFactory
import com.typesafe.config.ConfigFactory
import com.rabbitmq.client.{ ConnectionFactory => RabbitConnectionFactory }
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.nummulus.amqp.driver.DefaultProvider
import com.typesafe.config.Config
import com.typesafe.config.impl.SimpleConfigObject
import com.typesafe.config._
import akka.actor.ActorSystem

class ProviderConsumerFixture(fileName: String) {
  val config = ConfigFactory.load(fileName);
  val connectionFactory = new ConnectionFactory(new RabbitConnectionFactory)
  val driver = new DefaultDriver(connectionFactory, config)
  val consumer = driver.newAkkaConsumer("service.test", "Test")
  val provider = driver.newProvider("Test")
}