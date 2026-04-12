# EzFramework - Velocity

Note: this module depends on the Velocity API at runtime and declares it as `provided`.

## Usage (Velocity plugin)

EzFramework registers the messenger as a Velocity service on startup. You can
fetch it without casting the EzFramework plugin:

```java
VelocityEzMessenger messenger = VelocityMessengerServices.requireMessenger(proxy);

VelocityMessengerSetup.using(messenger)
	.registerPackets(ExamplePackets.BalanceRequest.class, ExamplePackets.BalanceResponse.class)
	.registerHandler(ExamplePackets.BalanceResponse.class, (packet, ctx) -> {
	    logger.info("Balance {} for {}", packet.balance, packet.playerId);
	});

// Optional: dispatch handlers off the event thread
messenger.setDispatchExecutor(task ->
	proxy.getScheduler().buildTask(this, task).schedule());

VelocityMessengerMetrics metrics = messenger.snapshotMetrics();
logger.info("Velocity messenger received {} packets", metrics.getReceiveCount());
```

To run the module tests locally or in CI, activate the Maven profile that injects the Velocity API onto the test classpath:

```bash
# run tests for the velocity module (profile supplies velocity-api for tests)
mvn -f ezframework-velocity/pom.xml -Pwith-velocity-api test
```

See the proxy docs for more details and examples: [docs/proxy/velocity.md](../docs/proxy/velocity.md)

In CI, enable the `with-velocity-api` profile so tests run successfully without bundling the API in the artifact.
