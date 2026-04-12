# Messenger (What & How)

The messenger is the central API used to send and receive structured cross-server
messages. It provides a registry, send helpers, handler registration and metrics.

Obtain messenger (Velocity):

```java
VelocityEzMessenger messenger = VelocityBootstrap.findMessenger(proxy).orElseThrow();
```

Obtain messenger (Bungee):

```java
BungeeEzMessenger messenger = ((BungeeBootstrap) getProxy().getPluginManager()
        .getPlugin("ezframework")).getMessenger();
```

Quick usage:

- Register your packet types on startup (see `packet-registration.md`).
- Register handlers for response or request types (see `handler-registration.md`).
- Use `messenger.send(serverName, packet)` to send to a specific backend.

Notes:

- The messenger is cross-version safe; use the provided bootstrap helpers.
- Packet classes must be registered on both proxy and backends.

See also:

- [packet-registration.md](packet-registration.md)
- [handler-registration.md](handler-registration.md)
- [channels.md](channels.md)
- [request-response.md](request-response.md)
- [retries-and-timeouts.md](retries-and-timeouts.md)
- [testing-and-debugging.md](testing-and-debugging.md)
