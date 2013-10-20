package com.nummulus.amqp.driver.akka

/**
 * Message acknowledging that a message from AMQP has been processed
 * succesfully and can be removed from the queue.
 * 
 * @param deliveryTag identifier of the message to acknowledge
 */
case class Acknowledge(deliveryTag: Long)