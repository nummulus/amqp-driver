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

class ProviderConsumerFixture() {
  val configuration = """
  amqp {
    host = localhost
    defines {
      serviceName = service.test
      Test {
        queue = Test
        durable = false
        exclusive = false
        autoDelete = false
        autoAcknowledge = false
      }
    }
    uses {
      service.test {
        serviceName = service.test
        Test {
          queue = Test
          durable = false
          exclusive = false
          autoDelete = false
          autoAcknowledge = true
        }
      }
    }
  }"""

  val config = ConfigFactory.parseString(configuration);
  val connectionFactory = new ConnectionFactory(new RabbitConnectionFactory)
  val driver = new DefaultDriver(connectionFactory, config)
  val consumer = driver.newConsumer("service.test", "Test")
  val provider = driver.newProvider("Test")

}