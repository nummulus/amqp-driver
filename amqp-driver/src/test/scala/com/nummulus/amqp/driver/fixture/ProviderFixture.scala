package com.nummulus.amqp.driver.fixture

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import com.nummulus.amqp.driver.Channel
import com.nummulus.amqp.driver.QueueDeclareOk
import com.nummulus.amqp.driver.configuration.QueueConfiguration
import com.nummulus.amqp.driver.DefaultProvider

trait ProviderFixture extends MockitoSugar {
  val channel = mock[Channel]
  val declareOk = mock[QueueDeclareOk]
  
  when (channel.queueDeclare) thenReturn declareOk
  when (channel.queueDeclare("requestQueue", true, false, false, null)) thenReturn declareOk
  
  val queueConfiguration = QueueConfiguration("requestQueue", true, false, false, true)
  
  val provider = new DefaultProvider(channel, queueConfiguration)
}