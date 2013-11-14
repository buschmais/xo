package com.buschmais.cdo.api;

import java.util.Map;

/**
 * Defines methods to manage the lifecycle of property instances, query execution and transaction management.
 */
public interface CdoManager {

    /**
     * Begin a transaction.
     */
    void begin();

    /**
     * Commit all changes of the current transaction.
     */
    void commit();

    /**
     * Rollback all changes from the current transaction.
     */
    void rollback();

    /**
     * Find all property instances according to the given type and value (e.g. from an index).
     *
     * @param <T>   The property type.
     * @param type  The interface of the property type.
     * @param value The value.
     * @return An {@Iterable} returning the property instance.
     */
    <T> Iterable<T> find(Class<T> type, Object value);

    /**
     * Create a new {@CompositeObject} instance.
     *
     * @param <T>   The expected return type. Note that it must be assignable to at least one of the interfaces specified for the types.
     * @param type  The interface the property type shall implement.
     * @param types Additional interfaces the property type shall implement.
     * @return The {@link CompositeObject} instance.
     */
    CompositeObject create(Class type, Class<?>... types);

    /**
     * Create a new property instance.
     *
     * @param <T>  The expected return type. Note that it must be assignable to at least one of the interfaces specified for the types.
     * @param type The interface the property type shall implement.
     * @return The property instance.
     */
    <T> T create(Class<T> type);

    /**
     * Migrates the type of a property instance to the given target types and returns it. The original instance will not be usable anymore after migration.
     *
     * @param <T>         The property type.
     * @param <M>         The migrated property type. Note that it be assignable to at least one of the interfaces specified for types.
     * @param instance    The instance.
     * @param targetType  The target interface  which shall be implemented by the migrated instance.
     * @param targetTypes More target interfaces which shall be implemented by the migrated instance.
     * @return The migrated instance.
     */
    <T, M> CompositeObject migrate(T instance, Class<M> targetType, Class<?>... targetTypes);

    /**
     * Migrates the type of a property instance to the given target type and returns it. The original instance will not be usable anymore after migration.
     *
     * @param <T>        The property type.
     * @param <M>        The migrated property type. Note that it be assignable to at least one of the interfaces specified for types.
     * @param instance   The instance.
     * @param targetType The target interface  which shall be implemented by the migrated instance.
     * @return The migrated instance.
     */
    <T, M> M migrate(T instance, Class<M> targetType);

    /**
     * Migrates the type of a property instance to the given target and returns it. The original instance will not be usable anymore after migration.
     *
     * @param <T>              The property type.
     * @param <M>              The migrated property type. Note that it be assignable to at least one of the interfaces specified for types.
     * @param instance         The instance.
     * @param migrationHandler The {@link MigrationHandler} to be used to migrate data (e.g. properties) to the new type.
     * @param targetType       The target interface which shall be implemented by the migrated instance.
     * @param targetTypes      More target interfaces which shall be implemented by the migrated instance.
     * @return The migrated instance.
     */
    <T, M> CompositeObject migrate(T instance, MigrationHandler<T, M> migrationHandler, Class<M> targetType, Class<?>... targetTypes);

    /**
     * Migrates the type of a property instance to the given target and returns it. The original instance will not be usable anymore after migration.
     *
     * @param <T>              The property type.
     * @param <M>              The migrated property type. Note that it be assignable to at least one of the interfaces specified for types.
     * @param instance         The instance.
     * @param migrationHandler The {@link MigrationHandler} to be used to migrate data (e.g. properties) to the new type.
     * @param targetType       The target interface which shall be implemented by the migrated instance.
     * @return The migrated instance.
     */
    <T, M> M migrate(T instance, MigrationHandler<T, M> migrationHandler, Class<M> targetType);

    /**
     * Deletes a property instance.
     *
     * @param <T>      The property type.
     * @param instance The instance.
     */
    <T> void delete(T instance);

    QueryResult executeQuery(String query);

    QueryResult executeQuery(String query, Map<String, Object> parameters);

    /**
     * Close the {@CdoManager}.
     */
    void close();

    interface MigrationHandler<T, M> {
        void migrate(T instance, M target);
    }
}
