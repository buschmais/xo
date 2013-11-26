package com.buschmais.cdo.neo4j.impl.query.proxy.method.object;

import com.buschmais.cdo.neo4j.impl.query.proxy.method.RowProxyMethod;

import java.util.Map;

public class ToStringMethod implements RowProxyMethod {

    @Override
    public Object invoke(Map<String, Object> entity, Object instance, Object[] args) {
        return "CompositeRowObject " + entity.toString();
    }

}
