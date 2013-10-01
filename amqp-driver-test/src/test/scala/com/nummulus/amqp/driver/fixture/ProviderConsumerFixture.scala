package com.nummulus.amqp.driver.fixture

import com.nummulus.amqp.driver.AmqpDriver

class ProviderConsumerFixture {
  val driver = AmqpDriver("ampq.conf")
  val consumer = driver.newConsumer("service.test", "Test")
  val provider = driver.newProvider("Test")
  provider
}