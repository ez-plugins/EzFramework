package com.skyblockexp.ezframework.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Converts {@link EzPacket} instances to and from {@code byte[]} using a JSON
 * envelope format. No platform-specific APIs are used.
 *
 * <h3>Wire format</h3>
 * <pre>
 * { "id": "economy.balance.request", "data": { ...packet fields... } }
 * </pre>
 *
 * <p>The envelope is encoded as UTF-8 bytes. The {@code "id"} field is used by
 * {@link EzPacketRegistry} to resolve the concrete class before the {@code "data"}
 * object is deserialized into it.
 *
 * <p>Instances are stateless and thread-safe.
 */
public final class EzSerializer {

    private static final String FIELD_ID = "id";
    private static final String FIELD_DATA = "data";

    private final Gson gson;

    /**
     * Create a serializer with default Gson settings.
     */
    public EzSerializer() {
        this.gson = new GsonBuilder().create();
    }

    /**
     * Create a serializer with a custom {@link Gson} instance (e.g. for
     * registering type adapters).
     *
     * @param gson pre-configured Gson instance; must not be null
     */
    public EzSerializer(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "gson");
    }

    /**
     * Serialize a packet to a UTF-8 encoded JSON envelope byte array.
     *
     * @param packet the packet to serialize; must not be null
     * @return the serialized bytes
     */
    public byte[] serialize(EzPacket packet) {
        Objects.requireNonNull(packet, "packet");
        JsonObject envelope = new JsonObject();
        envelope.addProperty(FIELD_ID, packet.packetId());
        envelope.add(FIELD_DATA, gson.toJsonTree(packet));
        return envelope.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Deserialize a UTF-8 encoded JSON envelope back into an {@link EzPacket}.
     *
     * @param data     the raw bytes received from the transport
     * @param registry the registry used to map the packet ID to a concrete class
     * @return the deserialized packet
     * @throws EzSerializerException if the bytes cannot be parsed, the ID is
     *                               unknown, or deserialization fails
     */
    public EzPacket deserialize(byte[] data, EzPacketRegistry registry) {
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(registry, "registry");
        try {
            String json = new String(data, StandardCharsets.UTF_8);
            JsonObject envelope = JsonParser.parseString(json).getAsJsonObject();
            String id = envelope.get(FIELD_ID).getAsString();
            // Validate namespace format so malformed wire messages are caught early
            try {
                EzPacketRegistry.requireNamespaced(id, "incoming wire message");
            } catch (IllegalArgumentException e) {
                throw new EzSerializerException("Malformed packet ID in incoming message: " + e.getMessage());
            }
            Class<? extends EzPacket> clazz = registry.getClass(id)
                    .orElseThrow(() -> new EzSerializerException("Unknown packet ID: " + id));
            return gson.fromJson(envelope.get(FIELD_DATA), clazz);
        } catch (EzSerializerException e) {
            throw e;
        } catch (Exception e) {
            throw new EzSerializerException("Failed to deserialize packet", e);
        }
    }

    /**
     * Thrown when serialization or deserialization fails.
     */
    public static final class EzSerializerException extends RuntimeException {

        /**
         * Constructs an exception with the given message.
         *
         * @param message detail message
         */
        public EzSerializerException(String message) {
            super(message);
        }

        /**
         * Constructs an exception with the given message and cause.
         *
         * @param message detail message
         * @param cause   underlying cause
         */
        public EzSerializerException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
