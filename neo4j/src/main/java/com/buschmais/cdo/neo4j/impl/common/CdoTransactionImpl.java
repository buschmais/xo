package com.buschmais.cdo.neo4j.impl.common;

import com.buschmais.cdo.api.CdoTransaction;
import com.buschmais.cdo.neo4j.spi.DatastoreSession;

import java.util.ArrayList;
import java.util.List;

public class CdoTransactionImpl implements CdoTransaction {

    private DatastoreSession.DatastoreTransaction datastoreTransaction;

    private List<Synchronization> synchronizations = new ArrayList<>();

    public CdoTransactionImpl(DatastoreSession.DatastoreTransaction datastoreTransaction) {
        this.datastoreTransaction = datastoreTransaction;
    }

    @Override
    public void begin() {
        datastoreTransaction.begin();
    }

    @Override
    public void commit() {
        beforeCompletion();
        boolean committed = false;
        try {
            datastoreTransaction.commit();
            committed = true;
        } finally {
            afterCompletion(committed);
        }
    }

    @Override
    public void rollback() {
        try {
            datastoreTransaction.rollback();
        } finally {
            afterCompletion(false);
        }
    }

    @Override
    public boolean isActive() {
        return datastoreTransaction.isActive();
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
        synchronizations.add(synchronization);
    }


    private void beforeCompletion() {
        executeSynchronizations(new SynchronizationOperation() {
            @Override
            public void run(Synchronization synchronization) {
                synchronization.beforeCompletion();
            }
        });
    }

    private void afterCompletion(final boolean committed) {
        executeSynchronizations(new SynchronizationOperation() {
            @Override
            public void run(Synchronization synchronization) {
                synchronization.afterCompletion(committed);
            }
        });
    }

    private void executeSynchronizations(SynchronizationOperation operation) {
        for (Synchronization synchronization : synchronizations) {
            operation.run(synchronization);
        }
    }

    private interface SynchronizationOperation {
        void run(Synchronization synchronization);
    }
}
