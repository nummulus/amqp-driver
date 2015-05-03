# Changelog

## 0.2.0 (snapshot)

### Consumer

#### Breaking Changes

* Removed the `AmqpConsumer` in favor of an actor consumer. Send it `AmqpConsumerRequest` messages. If you provide an optional `ActorRef`, it'll send back responses from a service as `AmqpConsumerResponse` messages.

### Provider

#### Breaking Changes

* Removed the `AmqpProvider` in favor of an actor provider. To bind an actor send Bind(ActorRef) to the provider. Unbinding is done by shutting down the previously bound actor.
* Renamed package `com.nummulus.amqp.driver.akka` to `com.nummulus.amqp.driver.api.provider` as it only contained provider functionality.
* Renamed `AmqpRequestMessage` to `AmqpProviderRequest` and `AmqpResponseMessage` to `AmqpProviderResponse`, both located in `com.nummulus.amqp.driver.api.provider`.
* Providers no longer create their own `ActorSystem`. The driver now lazily creates one which is shared by all providers and consumers.

#### New Features

* The `AmqpGuardianActor` is initialized asynchronously instead of synchronously.

### Other

* Moved `com.nummulus.amqp.driver.provider.AkkaMessageConsumer` to `com.nummulus.amqp.driver.akka`.
* Renamed `AmqpRequestMessageWithProperties` to `AmqpQueueMessageWithProperties`. It stayed in the `com.nummulus.amqp.driver.akka` package as it's not specifically tied to providers.
* Scala version upgraded from 2.10 to 2.11
* JDK upgraded from 1.7 to 1.8

## 0.1.0 (Oct 26, 2014)

* Initial release