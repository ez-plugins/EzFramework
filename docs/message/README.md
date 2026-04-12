# Messaging — EzFramework

This section describes the framework's messaging abstractions and conventions: the message provider API, formatting conventions (MiniMessage-style markup), and Minecraft color codes.

For **proxy ↔ backend plugin channels** (Velocity / BungeeCord, JSON packets), see the proxy overview: [Cross-server messaging](../proxy/overview.md) — that is a separate API from chat formatting here.

Recommended reading order:

- [Message provider API and registration](message_provider.md)
- [MiniMessage formatting & placeholders](mini_message.md)
- [Minecraft color codes reference](color_codes.md)

Use the messaging system to centralize user-facing text, support localization, and ensure consistent formatting across commands, managers, and storage responses.
