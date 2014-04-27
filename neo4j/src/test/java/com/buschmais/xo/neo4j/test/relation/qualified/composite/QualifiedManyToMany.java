package com.buschmais.xo.neo4j.test.relation.qualified.composite;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.buschmais.xo.neo4j.api.annotation.Relation;

@Relation("ManyToMany")
@Retention(RetentionPolicy.RUNTIME)
public @interface QualifiedManyToMany {
}
