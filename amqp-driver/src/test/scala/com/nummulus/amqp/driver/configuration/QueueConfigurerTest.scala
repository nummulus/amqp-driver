package com.nummulus.amqp.driver.configuration

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.typesafe.config.ConfigFactory

class QueueConfigurerTest extends FlatSpec with Matchers {
  val configValue = """amqp {
    host = localhost
    
    defines {
    
      serviceName = service.test
    
      operation_one {
        queue = one
        durable = false
        exclusive = false
        autoDelete = false
        autoAcknowledge = false
      }
    
      operation_two {
        queue = two
        durable = true
        exclusive = true
        autoDelete = true
        autoAcknowledge = true
      }
    }
    
    uses {
      service.test {
        serviceName = service.test
        operation_one {
          queue = one
          durable = true
          exclusive = true
          autoDelete = true
          autoAcknowledge = true
        }
    
        operation_two {
          queue = two
          durable = true
          exclusive = false
          autoDelete = true
          autoAcknowledge = false
        }
      }
      service.second {
        serviceName = service.second
        operation_one {
          queue = one
          durable = false
          exclusive = true
          autoDelete = false
          autoAcknowledge = true
        }      
      }
    }
  }"""

  val systemConfig = ConfigFactory.parseString(configValue)
  val rootConfig = systemConfig.getConfig("amqp")
  val queueConfigurer = new Object () with QueueConfigurer

  behavior of "QueueConfigurer"
  
  it should "give the provider a config with the correct attributes" in {
    val config = queueConfigurer.getProvideQueuerConfiguration(rootConfig, "operation_one")
    config should be (QueueConfiguration("service.test.one", durable = false, exclusive = false, autoDelete = false, autoAcknowledge = false))
  }

  it should "give two different provider operations the correct attributes" in {
    val config = queueConfigurer.getProvideQueuerConfiguration(rootConfig, "operation_two")
    config should be (QueueConfiguration("service.test.two", durable = true, exclusive = true, autoDelete = true, autoAcknowledge = true))
  }
  
  it should "give the consumer the correct attributes" in  {
    val config = queueConfigurer.getConsumerQueueConfiguration(rootConfig, "service.test", "operation_one")
    config should be (QueueConfiguration("service.test.one", durable = true, exclusive = true, autoDelete = true, autoAcknowledge = true))
  }
  
  it should "give the second operation the consumer consumes a correct configuration" in {
    val config = queueConfigurer.getConsumerQueueConfiguration(rootConfig, "service.test", "operation_two")
    config should be (QueueConfiguration("service.test.two", durable = true, exclusive = false, autoDelete = true, autoAcknowledge = false))
  }
  
  it should "be able to configure a consumer for a second service" in {
    val config = queueConfigurer.getConsumerQueueConfiguration(rootConfig, "service.second", "operation_one")
    config should be (QueueConfiguration("service.second.one", durable = false, exclusive = true, autoDelete = false, autoAcknowledge = true))
  }
}