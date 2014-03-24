package com.buschmais.xo.json.api;

import com.buschmais.xo.api.XOException;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.json.impl.JsonFileStore;
import com.buschmais.xo.json.impl.JsonFileStoreSession;
import com.buschmais.xo.json.impl.metadata.JsonNodeMetadata;
import com.buschmais.xo.json.impl.metadata.JsonRelationMetadata;
import com.buschmais.xo.spi.bootstrap.XODatastoreProvider;
import com.buschmais.xo.spi.datastore.Datastore;

import java.net.MalformedURLException;
import java.net.URI;

public class JsonFileStoreProvider implements XODatastoreProvider {

    @Override
    public Datastore<JsonFileStoreSession, JsonNodeMetadata, String, JsonRelationMetadata, String> createDatastore(XOUnit XOUnit) {
        URI uri = XOUnit.getUri();
        if (!"file".equals(uri.getScheme())) {
            throw new XOException("Only file URIs are supported by this store.");
        }
        try {
            return new JsonFileStore(uri.toURL().getPath());
        } catch (MalformedURLException e) {
            throw new XOException("Cannot convert URI '" + uri.toString() + "' to URL.", e);
        }
    }
}
