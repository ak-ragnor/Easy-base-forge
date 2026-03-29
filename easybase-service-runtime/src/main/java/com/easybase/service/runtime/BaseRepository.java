package com.easybase.service.runtime;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for all generated repository interfaces.
 *
 * <p>This interface intentionally hides Spring Data JPA from the service layer.
 * The generated {@code UserPersistenceAdapter} implements this interface and
 * delegates to a Spring Data {@code UserJpaRepository} internally.
 *
 * <p>The contract for {@link #deleteById(Object)} is deliberately abstract:
 * the persistence adapter decides whether to hard-delete or soft-delete based
 * on the {@code audit.softDelete} configuration at generation time.
 *
 * @param <T>  the domain model type (immutable record)
 * @param <ID> the primary key type
 */
public interface BaseRepository<T, ID> {

	/**
	 * Persists a new or updated domain model.
	 *
	 * @param entity the domain model to save
	 * @return the saved domain model
	 */
	T save(T entity);

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
	 * {@code audit.softDelete} in {@code easybase-config.yaml}.
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
