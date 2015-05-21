package com.nummulus.amqp.driver.configuration

import scala.concurrent.duration._

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.nummulus.amqp.driver.fixture.TimeOutConfigurerFixture

class TimeOutConfigurerTest extends FlatSpec with Matchers {
  behavior of "TimeOutConfigurer"

  it should "return the time-out specified by an operation" in new TimeOutConfigurerFixture {
    val timeOut = timeOutConfigurer.getConsumerTimeOut(rootConfigWith, "SlowService", "slowOperation")

    timeOut should be (300.millis)
  }

  it should "fall back to the service time-out if not specified by the operation" in new TimeOutConfigurerFixture {
    val timeOut = timeOutConfigurer.getConsumerTimeOut(rootConfigWith, "SlowService", "normalOperation")

    timeOut should be (200.millis)
  }

  it should "fall back to the global time-out if not specified by the operation" in new TimeOutConfigurerFixture {
    val timeOut = timeOutConfigurer.getConsumerTimeOut(rootConfigWith, "NormalService", "normalOperation")

    timeOut should be (100.millis)
  }

  it should "return infinite if no time-out is specified" in new TimeOutConfigurerFixture {
    val timeOut = timeOutConfigurer.getConsumerTimeOut(rootConfigWithout, "NormalService", "normalOperation")

    timeOut should be (Duration.Inf)
  }

  it should "return infinite if a time-out of 0 is specified" in new TimeOutConfigurerFixture {
    val timeOut = timeOutConfigurer.getConsumerTimeOut(rootConfigWith, "InfiniteService", "normalOperation")

    timeOut should be (Duration.Inf)
  }

  it should "return infinite if a negative time-out is specified" in new TimeOutConfigurerFixture {
    val timeOut = timeOutConfigurer.getConsumerTimeOut(rootConfigWith, "InfiniteService", "specialOperation")

    timeOut should be (Duration.Inf)
  }
}
