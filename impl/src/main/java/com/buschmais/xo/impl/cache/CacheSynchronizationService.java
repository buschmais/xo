package com.buschmais.xo.impl.cache;

import com.buschmais.xo.impl.SessionContext;
import com.buschmais.xo.impl.instancelistener.InstanceListenerService;
import com.buschmais.xo.spi.datastore.DatastoreEntityMetadata;
import com.buschmais.xo.spi.datastore.DatastoreRelationMetadata;
import com.buschmais.xo.spi.datastore.DatastoreSession;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

public class CacheSynchronizationService<Entity, Relation> {

    private final SessionContext<?, Entity, ?, ?, ?, Relation, ?, ?> sessionContext;

    public CacheSynchronizationService(SessionContext<?, Entity, ?, ?, ?, Relation, ?, ?> sessionContext) {
        this.sessionContext = sessionContext;
    }

    public void flush() {
        DatastoreSession<?, Entity, ? extends DatastoreEntityMetadata<?>, ?, ?, Relation, ? extends DatastoreRelationMetadata<?>, ?> datastoreSession = sessionContext.getDatastoreSession();
        InstanceListenerService instanceListenerService = sessionContext.getInstanceListenerService();
        for (Object instance : sessionContext.getRelationCache().writtenInstances()) {
            Relation relation = sessionContext.getRelationInstanceManager().getDatastoreType(instance);
            instanceListenerService.preUpdate(instance);
            validateInstance(instance);
            datastoreSession.flushRelation(relation);
            instanceListenerService.postUpdate(instance);
        }
        for (Object instance : sessionContext.getEntityCache().writtenInstances()) {
            Entity entity = sessionContext.getEntityInstanceManager().getDatastoreType(instance);
            instanceListenerService.preUpdate(instance);
            validateInstance(instance);
            datastoreSession.flushEntity(entity);
            instanceListenerService.postUpdate(instance);
        }
    }

    private void validateInstance(Object instance) {
        Set<ConstraintViolation<Object>> constraintViolations = sessionContext.getInstanceValidationService().validate(instance);
        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }
}