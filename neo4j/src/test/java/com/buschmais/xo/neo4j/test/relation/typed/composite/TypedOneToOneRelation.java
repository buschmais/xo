package com.buschmais.xo.neo4j.test.relation.typed.composite;

import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Relation("OneToOne")
public interface TypedOneToOneRelation extends TypedRelation {

    @Outgoing
    A getA();

    @Incoming
    B getB();

}
