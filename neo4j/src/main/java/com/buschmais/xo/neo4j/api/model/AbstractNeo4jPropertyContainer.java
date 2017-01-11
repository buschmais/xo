package com.buschmais.xo.neo4j.api.model;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;

import com.buschmais.xo.api.XOException;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public abstract class AbstractNeo4jPropertyContainer<T extends PropertyContainer>  {

    protected T delegate;

    private Object2ObjectOpenHashMap<String, Object> readCache = null;
    private Object2ObjectOpenHashMap<String, Object> writeCache = null;

    public AbstractNeo4jPropertyContainer(T delegate) {
        this.delegate = delegate;
    }

    public T getDelegate() {
        return delegate;
    }

    public boolean hasProperty(String key) {
        if (getReadCache().containsKey(key)) {
            return true;
        }
        return delegate.hasProperty(key);
    }

    public Object getProperty(String key) {
        Object2ObjectOpenHashMap<String, Object> readCache = getReadCache();
        if (readCache.containsKey(key)) {
            return readCache.get(key);
        }
        Object value = delegate.getProperty(key);
        readCache.put(key, value);
        return value;
    };

    public void setProperty(String key, Object value) {
        getWriteCache().put(key, value);
        getReadCache().put(key, value);
    }

    public Object removeProperty(String key) {
        getWriteCache().remove(key);
        getReadCache().remove(key);
        return delegate.removeProperty(key);
    }

    public void flush() {
        if (writeCache != null) {
            for (Map.Entry<String, Object> entry : writeCache.entrySet()) {
                delegate.setProperty(entry.getKey(), entry.getValue());
            }
        }
        writeCache = null;
    }

    public void clear() {
        readCache = null;
        writeCache = null;
    }

    private Object2ObjectOpenHashMap<String, Object> getReadCache() {
        if (readCache == null) {
            readCache = new Object2ObjectOpenHashMap<>();
        }
        return readCache;
    }

    private Object2ObjectOpenHashMap<String, Object> getWriteCache() {
        if (writeCache == null) {
            writeCache = new Object2ObjectOpenHashMap<>();
        }
        return writeCache;
    }

    @Override
    public final int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof AbstractNeo4jPropertyContainer<?>) {
            return delegate.equals(((AbstractNeo4jPropertyContainer<?>) obj).delegate);
        }
        return false;
    }

    @Override
    public final String toString() {
        return delegate.toString();
    }
}