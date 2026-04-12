# Serialization & Schema Compatibility

EzFramework provides `EzSerializer` for converting packet objects to bytes and
back. Keep compatibility and versioning in mind as you evolve packet classes.

Basic usage:

```java
EzSerializer ser = new EzSerializer();
byte[] out = ser.serialize(packet);
EzPacket incoming = ser.deserialize(data, registry);
```

Compatibility tips:

- Prefer adding optional fields with sensible defaults rather than removing or
  renaming fields.
- Use explicit version fields in packets when you expect breaking changes.
- Maintain a small migration layer if you need to deserialize older wire
  formats into new in-memory types.

Schema management
- Keep a single canonical registration sequence and reuse it on proxy and
  backends to avoid mismatches.
- Include tests that serialize and deserialize every packet to catch regressions.

See also:

- [packet-registration.md](packet-registration.md)
- [testing-and-debugging.md](testing-and-debugging.md)
