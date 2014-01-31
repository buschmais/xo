package com.buschmais.cdo.impl.proxy.entity.property;

import com.buschmais.cdo.impl.AbstractPropertyManager;
import com.buschmais.cdo.impl.SessionContext;
import com.buschmais.cdo.impl.proxy.common.property.AbstractPrimitivePropertySetMethod;
import com.buschmais.cdo.spi.metadata.method.PrimitivePropertyMethodMetadata;

public class PrimitivePropertySetMethod<Entity, Relation> extends AbstractPrimitivePropertySetMethod<Entity, Entity, Relation> {

    public PrimitivePropertySetMethod(SessionContext<?, Entity, ?, ?, ?, Relation, ?, ?> sessionContext, PrimitivePropertyMethodMetadata metadata) {
        super(sessionContext, metadata);
    }

    @Override
    protected AbstractPropertyManager<Entity, Entity, Relation> getPropertyManager() {
        return getSessionContext().getEntityPropertyManager();
    }
}