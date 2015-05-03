# AMQP driver for Akka

The driver integrates RabbitMQ with Akka, aimed at providing and consuming services.

## Examples

### Provider example

A service has one or more operations. Here is an "orderFood" operation for a "Waiter" service.

```scala
val driver = DefaultAmqpDriverFactory("amqp.conf")
val provider = driver.newProvider("orderFood")

provider ! Bind(system.actorOf(Props[WaiterActor])
```

By binding an actor the provider will start consuming messages from the queue and forward these to the hungry patron. In our case we'll respond by repeating the order as can be seen below.

```scala
class WaiterActor extends Actor {
  def receive = {
    case AmqpProviderRequest(message, tag) =>
      sender ! AmqpProviderResponse(s"You ordered $message", tag)
  }
}
```

### Consumer example

Consuming our waiter service is just as easy.

```scala
val waiter = driver.newConsumer("Waiter", "orderFood")
waiter ! AmqpConsumerRequest("Pizza with cheese", Some(hungryPatron))
```

This sends the message "Pizza with cheese" to the queue of the waiter. The response will be sent to the specified actorRef which will receive an `AmqpConsumerResponse`.

```scala
class HungryPatronActor extends Actor {
  def receive = {
    case AmqpConsumerResponse(message) =>
      println(message) // Will be "You ordered Pizza with cheese"
  }
}
```

## Installation

Note that AMQP driver requires at least Scala 2.11.

### Using Maven

AMQP driver is published to Maven Central. You only need to add the dependency to your POM file.
```xml
<dependency>
  <groupId>com.nummulus.amqp.driver</groupId>
  <artifactId>amqp-driver_2.11</artifactId>
  <version>0.2.0-SNAPSHOT</version>
</dependency>
```

### Using SBT

Add the dependency to your `build.sbt` file.

```scala
"com.nummulus.amqp.driver" %% "amqp-driver" % "0.2.0-SNAPSHOT"
```

## License

AMQP driver is licensed under the [Apache 2 license](./LICENSE).
