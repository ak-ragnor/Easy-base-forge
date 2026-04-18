package com.easybase.service.runtime;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for all generated repository interfaces.
 *
 * <p>This interface intentionally hides Spring Data JPA from the service layer.
 * The generated {@code *PersistenceAdapter} implements this interface and
 * delegates to a Spring Data {@code *JpaRepository} internally.
 *
 * <p>Create and update are separate operations so that lifecycle hooks can
 * distinguish between the two. The persistence adapter decides whether to
 * hard-delete or soft-delete based on the {@code softDelete} configuration.
 *
 * @param <T>  the domain model type (plain POJO)
 * @param <ID> the primary key type
 */
public interface BaseRepository<T, ID> {

	/**
	 * Persists a new domain model.
	 *
	 * @param entity the domain model to create
	 * @return the created domain model (may differ from input, e.g. generated ID)
	 */
	T create(T entity);

	/**
	 * Updates an existing domain model.
	 *
	 * @param id     the primary key of the entity to update
	 * @param entity the updated domain model
	 * @return the saved domain model after update
	 */
	T update(ID id, T entity);

	/**
	 * Finds a domain model by its primary key.
	 *
	 * <p>When soft-delete is enabled, only non-deleted records are returned.
	 *
	 * @param id the primary key to look up
	 * @return an {@link Optional} containing the domain model, or empty if not found
	 */
	Optional<T> findById(ID id);

	/**
	 * Returns all active domain models.
	 *
	 * <p>When soft-delete is enabled, deleted records are excluded.
	 *
	 * @return a list of domain models (never {@code null})
	 */
	List<T> findAll();

	/**
	 * Deletes the entity with the given primary key.
	 *
	 * <p>The implementation determines whether this is a hard delete or a soft delete
	 * (setting a {@code deleted} flag). This decision is made at generation time via
	 * {@code softDelete.enabled} configuration.
	 *
	 * @param id the primary key of the entity to delete
	 */
	void deleteById(ID id);

	/**
	 * Checks whether an entity with the given primary key exists.
	 *
	 * @param id the primary key to check
	 * @return {@code true} if the entity exists (and is not soft-deleted)
	 */
	boolean existsById(ID id);
}
