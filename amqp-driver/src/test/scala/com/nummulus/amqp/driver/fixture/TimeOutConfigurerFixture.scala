package com.nummulus.amqp.driver.fixture

import com.nummulus.amqp.driver.configuration.TimeOutConfigurer
import com.typesafe.config.ConfigFactory

trait TimeOutConfigurerFixture {
  private val configWith = ConfigFactory.parseString(readResource("/time-out/with.conf"))
  val rootConfigWith = configWith.getConfig("amqp")

  private val configWithout = ConfigFactory.parseString(readResource("/time-out/without.conf"))
  val rootConfigWithout = configWithout.getConfig("amqp")

  val timeOutConfigurer = new TimeOutConfigurer {}

  private def readResource(resource: String): String =
    scala.io.Source.fromInputStream(getClass.getResourceAsStream(resource)).mkString
}
