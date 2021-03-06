package com.g414.st9.proto.service.store;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Putting the "Key" in Key-Value storage.
 */
public class Key {
    private final String identifier;
    private final String encryptedIdentifier;
    private final String type;
    private final Long id;

    public Key(String type, Long id) throws Exception {
        this.identifier = type + ":" + Long.toString(id);
        this.encryptedIdentifier = KeyEncryptionHelper.encrypt(type, id);
        this.type = type;
        this.id = id;
    }

    public static Key valueOf(String key) throws Exception {
        if (key == null || key.length() == 0) {
            throw new WebApplicationException(Response
                    .status(Status.BAD_REQUEST).entity("Invalid key").build());
        }

        if (key.startsWith("@")) {
            return KeyEncryptionHelper.decrypt(key);
        }

        String[] parts = key.split(":");
        if (parts.length != 2) {
            throw new WebApplicationException(Response
                    .status(Status.BAD_REQUEST).entity("Invalid key").build());
        }

        Long id = Long.parseLong(parts[1]);

        return new Key(parts[0], id);
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getEncryptedIdentifier() {
        return encryptedIdentifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Key && identifier.equals(((Key) obj).identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public String toString() {
        return identifier;
    }
}
