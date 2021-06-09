package com.buschmais.xo.impl.proxy.query.object;

import java.util.Map;

import com.buschmais.xo.impl.proxy.query.RowProxyMethod;

public class EqualsMethod implements RowProxyMethod {

    @Override
    public Object invoke(Map<String, Object> entity, Object instance, Object[] args) {
        return entity.equals(args[0]);
    }
}
