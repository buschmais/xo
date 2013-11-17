package com.buschmais.cdo.neo4j.impl;

import com.buschmais.cdo.api.CdoException;
import com.buschmais.cdo.api.CompositeObject;
import com.buschmais.cdo.api.IterableResult;
import com.buschmais.cdo.api.Query;
import com.buschmais.cdo.neo4j.api.EmbeddedNeo4jCdoManager;
import com.buschmais.cdo.neo4j.impl.metadata.NodeMetadata;
import com.buschmais.cdo.neo4j.impl.metadata.NodeMetadataProvider;
import com.buschmais.cdo.neo4j.impl.metadata.PrimitivePropertyMethodMetadata;
import com.buschmais.cdo.neo4j.impl.proxy.AbstractIterableResult;
import com.buschmais.cdo.neo4j.impl.proxy.InstanceManager;
import com.buschmais.cdo.neo4j.impl.query.CypherStringQueryImpl;
import com.buschmais.cdo.neo4j.impl.query.CypherTypeQueryImpl;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EmbeddedNeo4jCdoManagerImpl implements EmbeddedNeo4jCdoManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedNeo4jCdoManagerImpl.class);

    private final NodeMetadataProvider nodeMetadataProvider;
    private final InstanceManager instanceManager;
    private final GraphDatabaseService database;
    private final ExecutionEngine executionEngine;
    private Transaction transaction;

    public EmbeddedNeo4jCdoManagerImpl(NodeMetadataProvider nodeMetadataProvider, GraphDatabaseService database, InstanceManager instanceManager) {
        this.nodeMetadataProvider = nodeMetadataProvider;
        this.database = database;
        this.instanceManager = instanceManager;
        this.executionEngine = new ExecutionEngine(database);
    }

    @Override
    public void begin() {
        transaction = database.beginTx();
    }

    @Override
    public void commit() {
        transaction.success();
        transaction.close();
    }

    @Override
    public void rollback() {
        transaction.failure();
        transaction.close();
    }

    @Override
    public <T> IterableResult<T> find(final Class<T> type, final Object value) {
        NodeMetadata nodeMetadata = nodeMetadataProvider.getNodeMetadata(type);
        Label label = nodeMetadata.getLabel();
        if (label == null) {
            throw new CdoException("Type " + type.getName() + " has no label.");
        }
        PrimitivePropertyMethodMetadata indexedProperty = nodeMetadata.getIndexedProperty();
        if (indexedProperty == null) {
            throw new CdoException("Type " + nodeMetadata.getType().getName() + " has no indexed property.");
        }
        final ResourceIterable<Node> nodesByLabelAndProperty = database.findNodesByLabelAndProperty(label, indexedProperty.getPropertyName(), value);
        return new AbstractIterableResult<T>() {
            @Override
            public Iterator<T> iterator() {
                final ResourceIterator<Node> iterator = nodesByLabelAndProperty.iterator();
                return new Iterator<T>() {

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public T next() {
                        Node node = iterator.next();
                        return instanceManager.getInstance(node);
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Cannot remove instance.");
                    }
                };
            }
        };
    }

    @Override
    public CompositeObject create(Class type, Class<?>... types) {
        Node node = database.createNode();
        List<Class<?>> effectiveTypes = getEffectiveTypes(type, types);
        Set<Label> labels = new HashSet<>();
        for (Class<?> currentType : effectiveTypes) {
            labels.addAll(nodeMetadataProvider.getNodeMetadata(currentType).getAggregatedLabels());
        }
        for (Label label : labels) {
            node.addLabel(label);
        }
        return (CompositeObject) instanceManager.getInstance(node, effectiveTypes);
    }

    public <T> T create(Class<T> type) {
        return create(type, new Class<?>[0]).as(type);
    }

    @Override
    public <T, M> CompositeObject migrate(T instance, MigrationHandler<T, M> migrationHandler, Class<M> targetType, Class<?>... targetTypes) {
        Node node = instanceManager.getNode(instance);
        List<Class<?>> types = instanceManager.getTypes(node);
        Set<Label> labels = new HashSet<>();
        for (Class<?> type : types) {
            NodeMetadata nodeMetadata = nodeMetadataProvider.getNodeMetadata(type);
            labels.addAll(nodeMetadata.getAggregatedLabels());
        }
        Set<Label> targetLabels = new HashSet<>();
        List<Class<?>> effectiveTargetTypes = getEffectiveTypes(targetType, targetTypes);
        for (Class<?> currentType : effectiveTargetTypes) {
            NodeMetadata targetMetadata = nodeMetadataProvider.getNodeMetadata(currentType);
            targetLabels.addAll(targetMetadata.getAggregatedLabels());
        }
        Set<Label> labelsToRemove = new HashSet<>(labels);
        labelsToRemove.removeAll(targetLabels);
        for (Label label : labelsToRemove) {
            node.removeLabel(label);
        }
        Set<Label> labelsToAdd = new HashSet<>(targetLabels);
        labelsToAdd.removeAll(labels);
        for (Label label : labelsToAdd) {
            node.addLabel(label);
        }
        instanceManager.removeInstance(instance);
        CompositeObject migratedInstance = instanceManager.getInstance(node);
        if (migrationHandler != null) {
            migrationHandler.migrate(instance, migratedInstance.as(targetType));
        }
        instanceManager.destroyInstance(instance);
        return migratedInstance;
    }

    @Override
    public <T, M> CompositeObject migrate(T instance, Class<M> targetType, Class<?>... targetTypes) {
        return migrate(instance, null, targetTypes);
    }

    @Override
    public <T, M> M migrate(T instance, MigrationHandler<T, M> migrationHandler, Class<M> targetType) {
        return migrate(instance, migrationHandler, targetType, new Class<?>[0]).as(targetType);
    }

    @Override
    public <T, M> M migrate(T instance, Class<M> targetType) {
        return migrate(instance, null, targetType);
    }


    @Override
    public <T> void delete(T instance) {
        Node node = instanceManager.getNode(instance);
        node.delete();
    }

    @Override
    public <QL> Query createQuery(QL query, Class<?>... types) {
        if (query instanceof String) {
            return new CypherStringQueryImpl(String.class.cast(query), executionEngine, instanceManager);
        } else if (query instanceof Class<?>) {
            return new CypherTypeQueryImpl(Class.class.cast(query), executionEngine, instanceManager);
        }
        throw new CdoException("Unsupported query language of type " + query.getClass().getName());
    }

    @Override
    public void close() {
        instanceManager.close();
    }

    @Override
    public GraphDatabaseService getGraphDatabaseService() {
        return database;
    }

    private List<Class<?>> getEffectiveTypes(Class<?> type, Class<?>... types) {
        List<Class<?>> effectiveTypes = new ArrayList<>(types.length + 1);
        effectiveTypes.add(type);
        effectiveTypes.addAll(Arrays.asList(types));
        return effectiveTypes;
    }
}
