package com.buschmais.xo.neo4j.test.transaction.composite;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("B")
public interface B {

    int getValue();

    void setValue(int value);

}
