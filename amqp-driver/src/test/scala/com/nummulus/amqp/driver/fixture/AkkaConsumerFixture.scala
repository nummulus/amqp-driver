package com.nummulus.amqp.driver.fixture

import scala.concurrent.duration.Duration

import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

import com.nummulus.amqp.driver.Channel
import com.nummulus.amqp.driver.DefaultConsumer
import com.nummulus.amqp.driver.IdGenerators
import com.nummulus.amqp.driver.QueueDeclareOk
import com.nummulus.amqp.driver.akka.AkkaMessageConsumer
import com.nummulus.amqp.driver.configuration.QueueConfiguration

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Scheduler
import akka.testkit.TestActorRef

class AkkaConsumerFixture(implicit system: ActorSystem) extends MockitoSugar {
  val channel = mock[Channel]
  val declareOk = mock[QueueDeclareOk]
  val messageConsumer = mock[AkkaMessageConsumer]
  val someCorrelationId = "4"
  val correlationIdGenerator = () => someCorrelationId
  val scheduler = mock[Scheduler]
  
  when (channel.queueDeclare()) thenReturn declareOk
  when (channel.queueDeclare("requestQueue", durable = true, exclusive = false, autoDelete = false, null)) thenReturn declareOk
  when (declareOk.getQueue) thenReturn "generated-queue-name"
  
  val queueConfiguration = QueueConfiguration("requestQueue", durable = true, exclusive = false, autoDelete = false, autoAcknowledge = true)
  
  val consumer = TestActorRef[DefaultConsumer](Props(classOf[DefaultConsumer], channel, queueConfiguration, Duration.Inf, correlationIdGenerator))

  def consumerWithTimeOut(timeOut: Duration) =
    TestActorRef[DefaultConsumer](Props(classOf[DefaultConsumerWithScheduler], channel, queueConfiguration, timeOut, correlationIdGenerator, scheduler))
}

private[driver] class DefaultConsumerWithScheduler(channel: Channel,
  configuration: QueueConfiguration,
  timeOut: Duration,
  generateId: IdGenerators.IdGenerator,
  testScheduler: Scheduler) extends DefaultConsumer(channel, configuration, timeOut, generateId) {

  override def scheduler: Scheduler = testScheduler
}