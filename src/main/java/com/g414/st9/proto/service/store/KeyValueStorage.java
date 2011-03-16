package com.g414.st9.proto.service.store;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

public interface KeyValueStorage {
    public abstract Integer getTypeId(String type) throws Exception;

    public abstract Response create(String type, String inValue, Long id)
            throws Exception;

    public abstract Response retrieve(String key) throws Exception;

    public abstract Response multiRetrieve(List<String> keys) throws Exception;

    public abstract Response update(String key, String inValue)
            throws Exception;

    public abstract Response delete(String key) throws Exception;

    public abstract void clear();

    public abstract Iterator<Map<String, Object>> iterator(String type)
            throws Exception;

    public abstract Response exportAll() throws Exception;
}