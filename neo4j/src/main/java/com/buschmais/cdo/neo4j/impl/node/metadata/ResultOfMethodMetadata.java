package com.buschmais.cdo.neo4j.impl.node.metadata;

import com.buschmais.cdo.neo4j.api.annotation.ResultOf;
import com.buschmais.cdo.neo4j.impl.common.reflection.BeanMethod;
import com.buschmais.cdo.neo4j.impl.common.reflection.UserDefinedMethod;

import java.util.List;

public class ResultOfMethodMetadata<DatastoreMetadata> extends AbstractMethodMetadata<BeanMethod, DatastoreMetadata> {

    private Class<?> query;

    private String usingThisAs;

    private List<ResultOf.Parameter> parameters;

    private boolean singleResult;

    public ResultOfMethodMetadata(BeanMethod beanMethod, Class<?> query, String usingThisAs, List<ResultOf.Parameter> parameters, boolean singleResult) {
        super(beanMethod, null);

        this.query = query;
        this.usingThisAs = usingThisAs;
        this.parameters = parameters;
        this.singleResult = singleResult;
    }

    public Class<?> getQuery() {
        return query;
    }

    public String getUsingThisAs() {
        return usingThisAs;
    }

    public List<ResultOf.Parameter> getParameters() {
        return parameters;
    }

    public boolean isSingleResult() {
        return singleResult;
    }
}
