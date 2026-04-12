# Wire format and packet registry

This page explains the JSON envelope format and registration rules used by
`EzSerializer` and `EzPacketRegistry`.

Wire envelope

Packets are transmitted as UTF-8 encoded JSON envelopes in this shape:

```json
{
  "id": "myplugin:action",
  "data": { /* packet fields */ }
}
```

- `id` must be a namespaced string in `pluginId:action` form (exactly one colon).
- `data` is serialized/deserialized via Gson into the concrete packet class.

Registration rules

- Use `EzPacketRegistry.register(Class)` to register packet classes used on your
  proxy or backend. The class must have a public no-arg constructor.
- Alternatively use `register(String id, Class)` to override the declared
  `packetId()` value.
- `EzSerializer.deserialize` validates the `id` field and uses the registry
  to resolve the concrete class.

Design notes

- Namespaced IDs prevent accidental collisions when multiple plugins share a
  single proxy messenger.
- Packet classes should be POJOs with public fields or getters/setters to
  integrate smoothly with Gson.

See also
- Overview: [overview.md](overview.md)
- Quick start: [quick_start.md](quick_start.md)
