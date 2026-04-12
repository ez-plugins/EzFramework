# Channels (Default & Custom)

EzFramework uses namespaced channels to avoid collisions. By default a shared
channel (EzChannel.DEFAULT) is used; you can also send messages on custom
channels when you need logical separation.

Send on default channel:

```java
messenger.send("survival", req); // uses EzChannel.DEFAULT implicitly
```

Send on a custom channel:

```java
messenger.send("survival", ServerMessage.of(req, new EzChannel("myplugin:data")));
```

Receiving code (backend):

```java
byte[] data = ...; // incoming plugin message payload
EzPacket pkt = new EzSerializer().deserialize(data, registry);
// inspect packet or channel metadata from incoming ServerMessage wrapper if used
```

Notes:

- Custom channels are useful to run multiple logical protocols over the same
  transport (e.g., analytics vs gameplay messages).
- Ensure both sender and receiver agree on channel names when using custom
  channels.

See also:

- [messenger.md](messenger.md)
- [packet-registration.md](packet-registration.md)
- [request-response.md](request-response.md)
