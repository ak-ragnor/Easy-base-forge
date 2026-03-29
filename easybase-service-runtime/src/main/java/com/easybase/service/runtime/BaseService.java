package com.easybase.service.runtime;

import java.util.List;
import java.util.Optional;

/**
 * Core service contract for all generated service base classes.
 *
 * <p>Defines the five standard CRUD operations using underscore-prefixed names
 * to clearly distinguish framework-level methods from developer-defined business methods.
 * Developers never call these directly — they call their own business-named methods
 * on {@code UserService}, which delegates to these internally.
 *
 * @param <T>  the domain model type (immutable record)
 * @param <ID> the primary key type
 */
public interface BaseService<T, ID> {

	/**
	 * Persists a new entity and fires {@code beforeCreate}/{@code afterCreate} hooks.
	 *
	 * @param entity the domain model to persist
	 * @return the saved domain model (may differ from input, e.g. generated ID)
	 */
	T _create(T entity);

	/**
	 * Updates an existing entity and fires {@code beforeUpdate}/{@code afterUpdate} hooks.
	 *
	 * @param id     the primary key of the entity to update
	 * @param entity the updated domain model
	 * @return the saved domain model after update
	 */
	T _update(ID id, T entity);

	/**
	 * Deletes an entity (hard or soft depending on configuration) and fires
	 * {@code beforeDelete}/{@code afterDelete} hooks.
	 *
	 * @param id the primary key of the entity to delete
	 */
	void _delete(ID id);

	/**
	 * Retrieves a single entity by its primary key.
	 *
	 * @param id the primary key to look up
	 * @return an {@link Optional} containing the domain model, or empty if not found
	 */
	Optional<T> _get(ID id);

	/**
	 * Retrieves all entities.
	 *
	 * @return a list of domain models (never {@code null}; may be empty)
	 */
	List<T> _list();
}
