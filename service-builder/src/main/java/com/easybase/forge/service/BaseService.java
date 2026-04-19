package com.easybase.forge.service;

import java.util.List;
import java.util.Optional;

/**
 * Core service contract for all generated service base classes.
 *
 * <p>Defines the five standard CRUD operations. These framework-level methods are
 * implemented by the generated {@code *ServiceBaseImpl} and should not be called
 * directly by application code — use the developer-owned business methods on
 * {@code *LocalService} or {@code *Service} instead.
 *
 * @param <T>  the domain model type (plain POJO)
 * @param <ID> the primary key type
 */
public interface BaseService<T, ID> {

	/**
	 * Persists a new entity.
	 *
	 * @param entity the domain model to persist
	 * @return the saved domain model (may differ from input, e.g. generated ID)
	 */
	T create(T entity);

	/**
	 * Updates an existing entity.
	 *
	 * @param id     the primary key of the entity to update
	 * @param entity the updated domain model
	 * @return the saved domain model after update
	 */
	T update(ID id, T entity);

	/**
	 * Deletes an entity (hard or soft depending on configuration).
	 *
	 * @param id the primary key of the entity to delete
	 */
	void delete(ID id);

	/**
	 * Retrieves a single entity by its primary key.
	 *
	 * @param id the primary key to look up
	 * @return an {@link Optional} containing the domain model, or empty if not found
	 */
	Optional<T> findById(ID id);

	/**
	 * Retrieves all entities.
	 *
	 * @return a list of domain models (never {@code null}; may be empty)
	 */
	List<T> findAll();
}
