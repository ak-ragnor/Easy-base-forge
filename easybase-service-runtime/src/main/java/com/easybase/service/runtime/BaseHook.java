package com.easybase.service.runtime;

/**
 * Lifecycle hook contract for all generated hook interfaces.
 *
 * <p>All methods have default no-op implementations so that developers only need to override
 * the lifecycle events they actually care about. Multiple hook implementations can be registered
 * as Spring beans and will all be invoked in order by the generated
 * {@code *PersistenceAdapterBase}.
 *
 * @param <T>  the domain model type (plain POJO)
 * @param <ID> the primary key type
 */
public interface BaseHook<T, ID> {

	/**
	 * Called before an entity is saved (create or update).
	 *
	 * @param entity the domain model about to be saved
	 */
	default void beforeSave(T entity) {}

	/**
	 * Called after an entity has been saved (create or update).
	 *
	 * @param entity the saved domain model (includes generated ID and audit timestamps)
	 */
	default void afterSave(T entity) {}

	/**
	 * Called before an existing entity is updated.
	 *
	 * @param id     the primary key of the entity being updated
	 * @param entity the updated domain model about to be persisted
	 */
	default void beforeUpdate(ID id, T entity) {}

	/**
	 * Called after an existing entity has been updated.
	 *
	 * @param entity the saved domain model after update
	 */
	default void afterUpdate(T entity) {}

	/**
	 * Called before an entity is deleted (hard or soft).
	 *
	 * @param id the primary key of the entity about to be deleted
	 */
	default void beforeDelete(ID id) {}

	/**
	 * Called after an entity has been deleted (hard or soft).
	 *
	 * @param id the primary key of the entity that was deleted
	 */
	default void afterDelete(ID id) {}
}
