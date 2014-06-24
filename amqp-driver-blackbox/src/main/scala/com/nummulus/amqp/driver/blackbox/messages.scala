package com.nummulus.amqp.driver.blackbox

import scala.concurrent.Promise

private[blackbox] sealed trait BlackBoxMessage
private[blackbox] case class TellMessage(message: String)
private[blackbox] case class AskMessage(message: String, promise: Promise[String])
