package com.buschmais.xo.neo4j.test.batchable.composite;

import com.buschmais.xo.neo4j.api.annotation.Batchable;
import com.buschmais.xo.neo4j.api.annotation.Indexed;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("A")
@Batchable(true)
public interface A {

    @Indexed
    String getName();

    void setName(String name);

    A2B getA2B();

}
