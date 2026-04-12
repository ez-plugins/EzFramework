# Advanced message options — Cross-server messaging

This page documents advanced message options and runtime behaviors available
to EzFramework users when building cross-server packet flows. For platform
integration examples see the Velocity and Bungee guides:

- Velocity: [velocity.md](velocity.md)
- Bungee: [bungee.md](bungee.md)

1) Channels and `EzChannel`

- Default channel: `EzChannel.DEFAULT` (`ezframework:channel`). Use the
    convenience `EzMessenger.send(String, EzPacket)` /
    `broadcast(EzPacket)` overloads to send on the default channel.
- Custom channels: construct `new EzChannel("namespace:key")` and wrap a
    packet with `ServerMessage.of(packet, ezChannel)` when you need separate
    transport namespaces or different routing semantics.

2) Delivery semantics and queued messages

- Proxy transports use platform plugin messaging. Delivery depends on the proxy and backend: messages may be queued, dropped, or fail if no player carries the link to the backend. Expect these behaviors:
  - Velocity: `sendPluginMessage` returns whether bytes were sent; a false
      result indicates no connected player on that server and message may be
      queued or dropped by the proxy.
  - Bungee: plugin messages require an active player connection; sending to
      empty servers may fail. Handle this by checking return values or using
      application-level retries.

3) Serialization and custom types

- `EzSerializer` uses Gson. To support custom field types, construct a
    `new EzSerializer(gson)` with registered type adapters (e.g. `TypeAdapter`,
    `JsonSerializer`). Ensure both proxy and backend use compatible Gson
    configurations.
- The wire envelope contains `"id"` and `"data"`. The `id` is validated
    for the `pluginId:action` format before deserialization; unknown IDs throw
    `EzSerializer.EzSerializerException`.

4) Packet registration and versioning

- Register packet classes in `EzPacketRegistry` on both proxy and backends.
    Use `register(String id, Class)` to override the declared ID when needed.
- For schema evolution prefer additive changes (new optional fields). When
    changing field names or types, maintain backward-compatibility by either
    using custom Gson adapters or creating a new packet ID.

5) Handler registration and threading

- Register handlers via `EzMessenger.registerHandler(Class<T>, EzPacketHandler<T>)`.
- By default handlers run on the plugin/event thread. For heavier work you
    can offload execution using `VelocityEzMessenger#setDispatchExecutor(Executor)`
    (Velocity) or an equivalent `Executor` wrapper in your transport. When
    using async dispatch, be mindful of thread-safety and proxy API rules.

6) Observability and metrics

- `VelocityEzMessenger` exposes a `snapshotMetrics()` helper that reports
    counters for sends, broadcasts, receives, deserialization failures, and
    handler errors. Consider emitting these metrics to your monitoring system
    for production visibility.

7) Error handling patterns

- Deserialization failures: log and drop the message; increment the
    deserialize-failure counter and surface alerts for repeated failures.
- Missing handler: log at debug and ignore — useful when multiple plugins
    share the same messenger but not all packet types.
- Handler exceptions: catch and log; if needed, publish an error packet back
    to the origin server using the `sourceServer` in `EzContext`.

8) Best practices

- Use `EzPacketNamespace` to build packet IDs and avoid collisions.
- Keep packet classes lightweight POJOs with public no-arg constructors for
    stable Gson deserialization.
- Favor explicit error-response packets instead of throwing for recoverable
    errors; this keeps cross-server flows observable and debuggable.

9) Platform-specific notes

- See [velocity.md](velocity.md) for Velocity-specific dispatch, scheduler
    examples, and the cross-version lookup helpers.
- See [bungee.md](bungee.md) for BungeeCord notes about player-connected
    delivery semantics.

If you want, I can add small example snippets for retrying failed sends or
for integrating metrics with Prometheus/Grafana.
