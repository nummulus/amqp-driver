package com.nummulus.amqp.driver.fixture

import org.mockito.Mockito._
import com.nummulus.amqp.driver.consumer.BlockingMessageConsumer
import com.nummulus.amqp.driver.QueueDeclareOk
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.nummulus.amqp.driver.consumer.CorrelationIdGenerator
import com.nummulus.amqp.driver.DefaultConsumer
import com.nummulus.amqp.driver.Channel
import org.scalatest.mock.MockitoSugar

trait ConsumerFixture extends MockitoSugar {
  val channel = mock[Channel]
  val declareOk = mock[QueueDeclareOk]
  val messageConsumer = mock[BlockingMessageConsumer]
  val correlationIdGenerator = mock[CorrelationIdGenerator]
  
  when (channel.queueDeclare) thenReturn declareOk
  when (channel.queueDeclare("requestQueue", true, false, false, null)) thenReturn declareOk
  when (declareOk.getQueue) thenReturn "generated-queue-name"
  when (correlationIdGenerator.generate) thenReturn "4"
  
  val queueConfiguration = QueueConfiguration("requestQueue", true, false, false, true)
  
  val consumer = new DefaultConsumer(channel, queueConfiguration, messageConsumer, correlationIdGenerator)
}