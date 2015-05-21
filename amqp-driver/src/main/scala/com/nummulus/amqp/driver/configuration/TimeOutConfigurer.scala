package com.nummulus.amqp.driver.configuration

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration

import com.typesafe.config.Config
import com.typesafe.config.ConfigException

private[driver] trait TimeOutConfigurer {
  def getConsumerTimeOut(rootConfig: Config, service: String, operation: String): Duration = try {
    val serviceConfig = rootConfig.getConfig(s"uses.$service")
    val operationConfig = serviceConfig.getConfig(operation)
      .withFallback(serviceConfig)
      .withFallback(rootConfig)

    operationConfig.getDuration("consumerTimeOut", TimeUnit.MILLISECONDS) match {
      case timeOut if timeOut > 0 => Duration(timeOut, TimeUnit.MILLISECONDS)
      case timeOut => Duration.Inf
    }
  } catch {
    case e: ConfigException.Missing => Duration.Inf
  }
}
