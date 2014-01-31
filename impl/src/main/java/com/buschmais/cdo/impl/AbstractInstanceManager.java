package com.buschmais.cdo.impl;

import com.buschmais.cdo.api.CompositeObject;
import com.buschmais.cdo.impl.cache.TransactionalCache;
import com.buschmais.cdo.impl.proxy.ProxyMethodService;
import com.buschmais.cdo.impl.proxy.entity.InstanceInvocationHandler;
import com.buschmais.cdo.spi.datastore.TypeMetadataSet;

/**
 * Abstract base implementation of an instance manager.
 * <p>It provides functionality to map the lifecycle of proxy instances to their corresponding datastore type.</p>
 *
 * @param <DatastoreId>   The id type of the datastore type.
 * @param <DatastoreType> The datastore type.
 */
public abstract class AbstractInstanceManager<DatastoreId, DatastoreType> {

    private final TransactionalCache<DatastoreId> cache;
    private final ProxyFactory proxyFactory;

    /**
     * Constructor.
     *
     * @param cache        The transactional cache.
     * @param proxyFactory The proxy factory.
     */
    public AbstractInstanceManager(TransactionalCache<DatastoreId> cache, ProxyFactory proxyFactory) {
        this.cache = cache;
        this.proxyFactory = proxyFactory;
    }

    /**
     * Return the proxy instance which corresponds to the given datastore type.
     *
     * @param datastoreType The datastore type.
     * @param <T>           The instance type.
     * @return The instance.
     */
    public <T> T getInstance(DatastoreType datastoreType) {
        DatastoreId id = getDatastoreId(datastoreType);
        TypeMetadataSet<?> types = getTypes(datastoreType);
        Object instance = cache.get(id);
        if (instance == null) {
            InstanceInvocationHandler invocationHandler = new InstanceInvocationHandler(datastoreType, getProxyMethodService());
            instance = proxyFactory.createInstance(invocationHandler, types.toClasses(), CompositeObject.class);
            cache.put(id, instance);
        }
        return (T) instance;
    }

    /**
     * Removes an instance, e.g. before deletion or migration.
     *
     * @param instance   The instance.
     * @param <Instance> The instance type.
     */
    public <Instance> void removeInstance(Instance instance) {
        DatastoreType datastoreType = getDatastoreType(instance);
        DatastoreId id = getDatastoreId(datastoreType);
        cache.remove(id);
    }

    /**
     * Destroys an instance, i.e. makes it unusable-
     *
     * @param instance   The instance.
     * @param <Instance> The instance type.
     */
    public <Instance> void destroyInstance(Instance instance) {
        proxyFactory.getInvocationHandler(instance).close();
    }

    /**
     * Determine if a given instance is a datastore type handled by this manager.
     *
     * @param instance   The instance.
     * @param <Instance> The instance type.
     * @return <code>true</code> if the instance is handled by this manager.
     */
    public <Instance> boolean isInstance(Instance instance) {
        if (instance instanceof CompositeObject) {
            Object delegate = ((CompositeObject) instance).getDelegate();
            return isDatastoreType(delegate);
        }
        return false;
    }

    /**
     * Return the datastore type represented by an instance.
     *
     * @param instance   The instance.
     * @param <Instance> The instance type.
     * @return The corresponding datastore type.
     */
    public <Instance> DatastoreType getDatastoreType(Instance instance) {
        InstanceInvocationHandler<DatastoreType> invocationHandler = proxyFactory.getInvocationHandler(instance);
        DatastoreType datastoreType = invocationHandler.getDatastoreType();
        return datastoreType;
    }

    /**
     * Closes this manager instance.
     */
    public void close() {
        for (Object instance : cache.values()) {
            destroyInstance(instance);
        }
        cache.clear();
    }

    /**
     * Determine if a given object is a datastore type.
     *
     * @param o The object
     * @return <code>true</code> If the given object is a datastore type.
     */
    protected abstract boolean isDatastoreType(Object o);

    /**
     * Return the unique id of a datastore type.
     *
     * @param datastoreType The datastore type.
     * @return The id.
     */
    protected abstract DatastoreId getDatastoreId(DatastoreType datastoreType);

    /**
     * Determines the {@link com.buschmais.cdo.spi.datastore.TypeMetadataSet} of a datastore type.
     *
     * @param datastoreType The datastore type.
     * @return The {@link com.buschmais.cdo.spi.datastore.TypeMetadataSet}.
     */
    protected abstract TypeMetadataSet<?> getTypes(DatastoreType datastoreType);

    /**
     * Return the {@link com.buschmais.cdo.impl.proxy.ProxyMethodService} associated with this manager.
     *
     * @return The {@link com.buschmais.cdo.impl.proxy.ProxyMethodService}.
     */
    protected abstract ProxyMethodService<DatastoreType, ?> getProxyMethodService();
}