# Bungee: Empty servers and delivery

BungeeCord plugin messaging requires an active player connection to deliver
plugin messages to a backend server. If a target server has no players, messages
may not be delivered.

Workarounds

- Ensure at least one 'relay' player or system connection is present on every
  backend that must receive messages.
- Use an alternate transport (e.g., direct socket or a shared datastore) for
  critical messages that cannot await players.
- On proxies, track server population and fail fast with a clear error if the
  destination is empty:

```java
if (proxy.getServer("survival").getPlayersConnected().isEmpty()) {
  // decide: queue, drop, or reply with error
}
```

Notes
- Velocity and more modern proxy APIs may offer different guarantees; consult
  platform docs for delivery semantics.

See also:

- [messenger.md](messenger.md)
- [testing-and-debugging.md](testing-and-debugging.md)
