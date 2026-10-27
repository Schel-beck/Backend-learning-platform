package app.controllers;

import io.javalin.http.Context;

/**
 * Interface defining the common operations for controllers.
 *
 * @param <T> the type of object used for validation
 * @param <D> the type of the primary key
 */
public interface IController<T, D> {

    /**
     * Retrieves a single entity based on the request.
     *
     * @param ctx the Javalin context containing the request
     */
    void read(Context ctx);

    /**
     * Retrieves all entities.
     *
     * @param ctx the Javalin context containing the request
     */
    void readAll(Context ctx);

    /**
     * Creates a new entity from the request.
     *
     * @param ctx the Javalin context containing the request data
     */
    void create(Context ctx);

    /**
     * Updates an existing entity from the request.
     *
     * @param ctx the Javalin context containing the request data
     */
    void update(Context ctx);

    /**
     * Deletes an existing entity based on the request.
     *
     * @param ctx the Javalin context containing the request
     */
    void delete(Context ctx);

    /**
     * Validates whether a primary key is valid.
     *
     * @param d the primary key to validate
     * @return true if the primary key is valid, otherwise false
     */
    boolean validatePrimaryKey(D d);

    /**
     * Validates the entity data received in the request.
     *
     * @param ctx the Javalin context containing the request data
     * @return the validated object
     */
    T validateEntity(Context ctx);
}