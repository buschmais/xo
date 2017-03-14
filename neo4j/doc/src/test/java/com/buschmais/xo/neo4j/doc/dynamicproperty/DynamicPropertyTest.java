package com.buschmais.xo.neo4j.doc.dynamic;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.doc.AbstractDocumentationTest;

public class DynamicPropertyTest extends AbstractDocumentationTest {

    @Override
    protected void configure(XOUnit.XOUnitBuilder builder) {
        builder.type(Person.class).type(Actor.class).type(Movie.class);
    }

    @Test
    public void dynamicProperty() throws URISyntaxException, IOException {
        xoManager.currentTransaction().begin();

        Movie movie = xoManager.create(Movie.class);
        Actor actor = xoManager.create(Actor.class);
        actor.setAge(42);
        movie.getActors().add(actor);

        Long totalActors= movie.getActorCount();
        Long actorsWithAge42 = movie.getActorCountByAge(42);

        xoManager.currentTransaction().commit();
    }

}