# Cross-server messaging — Overview

EzFramework provides a lightweight, platform-agnostic API for sending structured
messages between a proxy (Velocity or BungeeCord) and backend servers. The
implementation is intentionally split into a core protocol module and optional
transport modules so projects only depend on what they need.

- `ezframework-core` contains the protocol types and serializer; no proxy APIs are referenced.
- `ezframework-velocity` and `ezframework-bungee` implement transports for the corresponding proxies.
- Packets are identified by namespaced IDs (`pluginId:action`) to avoid collisions between plugins.

Key points
- `ezframework-core` contains the protocol types and serializer; no proxy APIs are referenced.
- `ezframework-velocity` and `ezframework-bungee` implement transports for the corresponding proxies.
- Packets are identified by namespaced IDs (`pluginId:action`) to avoid collisions between plugins.

Related pages
- Wire format: [wire_format.md](wire_format.md)
- Quick start: [quick_start.md](quick_start.md)
- Velocity integration: [velocity.md](velocity.md)
- Bungee integration: [bungee.md](bungee.md)
- `ezframework-core` contains the protocol types and serializer; no proxy APIs are referenced.
- `ezframework-velocity` and `ezframework-bungee` implement transports for the corresponding proxies.
- Packets are identified by namespaced IDs (`pluginId:action`) to avoid collisions between plugins.

Components: [component/README.md](component/README.md)

Recommended reading order
1. `wire_format.md` — how packets are encoded and validated
2. `quick_start.md` — minimal setup and examples
3. `velocity.md` / `bungee.md` — platform specifics and examples
