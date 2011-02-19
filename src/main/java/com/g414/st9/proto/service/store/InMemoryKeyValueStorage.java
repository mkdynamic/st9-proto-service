package com.g414.st9.proto.service.store;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;

/**
 * In-memory implementation of key-value storage.
 */
public class InMemoryKeyValueStorage implements KeyValueStorage {
    private final ConcurrentHashMap<String, byte[]> storage = new ConcurrentHashMap<String, byte[]>();
    private final ConcurrentHashMap<String, AtomicLong> sequences = new ConcurrentHashMap<String, AtomicLong>();

    /**
     * @see com.g414.st9.proto.service.store.KeyValueStorage#create(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public Response create(String type, String inValue) throws Exception {
        try {
            validateType(type);
            Map<String, Object> readValue = EncodingHelper
                    .parseJsonString(inValue);
            readValue.remove("id");
            readValue.remove("kind");

            String key = type + ":" + this.nextId(type);
            Map<String, Object> value = new LinkedHashMap<String, Object>();
            value.put("id", key);
            value.put("kind", type);
            value.putAll(readValue);

            String valueJson = EncodingHelper.convertToJson(value);

            value.remove("id");
            value.remove("kind");

            byte[] valueBytes = EncodingHelper.convertToSmileLzf(value);

            storage.put(key, valueBytes);

            return Response.status(Status.OK).entity(valueJson).build();
        } catch (WebApplicationException e) {
            return e.getResponse();
        }
    }

    /**
     * @see com.g414.st9.proto.service.store.KeyValueStorage#retrieve(java.lang.String)
     */
    @Override
    public Response retrieve(String key) throws Exception {
        try {
            Object[] keyParts = KeyHelper.validateKey(key);

            byte[] valueBytesLzf = storage.get(key);

            if (valueBytesLzf == null) {
                return Response.status(Status.NOT_FOUND).entity("").build();
            }

            LinkedHashMap<String, Object> readValue = (LinkedHashMap<String, Object>) EncodingHelper
                    .parseSmileLzf(valueBytesLzf);
            readValue.remove("id");
            readValue.remove("kind");

            Map<String, Object> value = new LinkedHashMap<String, Object>();
            value.put("id", key);
            value.put("kind", keyParts[0]);
            value.putAll(readValue);

            String jsonValue = EncodingHelper.convertToJson(value);

            return Response.status(Status.OK).entity(jsonValue).build();
        } catch (WebApplicationException e) {
            return e.getResponse();
        }
    }

    /**
     * @see com.g414.st9.proto.service.store.KeyValueStorage#multiRetrieve(java.lang.String)
     */
    @Override
    public Response multiRetrieve(List<String> keys) throws Exception {
        try {
            if (keys == null || keys.isEmpty()) {
                Response.status(Status.OK)
                        .entity(EncodingHelper.convertToJson(Collections
                                .<String, Object> emptyMap())).build();
            }

            LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

            for (String key : keys) {
                Object[] keyParts = KeyHelper.validateKey(key);
                
                byte[] valueBytesLzf = storage.get(key);

                if (valueBytesLzf == null) {
                    result.put(key, null);

                    continue;
                }

                LinkedHashMap<String, Object> readValue = (LinkedHashMap<String, Object>) EncodingHelper
                        .parseSmileLzf(valueBytesLzf);
                readValue.remove("id");
                readValue.remove("kind");

                Map<String, Object> value = new LinkedHashMap<String, Object>();
                value.put("id", key);
                value.put("kind", keyParts[0]);
                value.putAll(readValue);

                result.put(key, value);
            }

            String jsonValue = EncodingHelper.convertToJson(result);

            return Response.status(Status.OK).entity(jsonValue).build();
        } catch (WebApplicationException e) {
            return e.getResponse();
        }
    }

    /**
     * @see com.g414.st9.proto.service.store.KeyValueStorage#update(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public Response update(String key, String inValue) throws Exception {
        Object[] keyParts = KeyHelper.validateKey(key);
        
        Map<String, Object> readValue = EncodingHelper.parseJsonString(inValue);
        readValue.remove("id");
        readValue.remove("kind");
        
        Map<String, Object> value = new LinkedHashMap<String, Object>();
        value.put("id", key);
        value.put("kind", keyParts[0]);
        value.putAll(readValue);

        if (!storage.containsKey(key)) {
            return Response.status(Status.NOT_FOUND).entity("").build();
        }

        String valueJson = EncodingHelper.convertToJson(value);

        value.remove("id");
        value.remove("kind");

        byte[] valueBytes = EncodingHelper.convertToSmileLzf(value);

        storage.put(key, valueBytes);

        return Response.status(Status.OK).entity(valueJson).build();
    }

    /**
     * @see com.g414.st9.proto.service.store.KeyValueStorage#delete(java.lang.String)
     */
    @Override
    public Response delete(String key) throws Exception {
        KeyHelper.validateKey(key);

        if (!storage.containsKey(key)) {
            return Response.status(Status.NOT_FOUND).entity("").build();
        }

        storage.remove(key);

        return Response.status(Status.NO_CONTENT).entity("").build();
    }

    /**
     * @see com.g414.st9.proto.service.store.KeyValueStorage#clear()
     */
    @Override
    public void clear() {
        this.sequences.clear();
        this.storage.clear();
    }

    private Long nextId(String type) {
        validateType(type);

        AtomicLong aNewSeq = new AtomicLong(0);
        AtomicLong existing = sequences.putIfAbsent(type, aNewSeq);

        if (existing == null) {
            existing = aNewSeq;
        }

        return existing.incrementAndGet();
    }

    private static void validateType(String type) {
        if (type == null || type.length() == 0 || type.indexOf(":") != -1) {
            throw new WebApplicationException(Response
                    .status(Status.BAD_REQUEST).entity("Invalid entity 'type'")
                    .build());
        }
    }

    public static class InMemoryKeyValueStorageModule extends AbstractModule {
        @Override
        public void configure() {
            Binder binder = binder();

            binder.bind(KeyValueStorage.class)
                    .to(InMemoryKeyValueStorage.class).asEagerSingleton();
        }
    }
}