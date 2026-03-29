package com.easybase.service.runtime;

/**
 * Lifecycle hook contract for all generated hook interfaces.
 *
 * <p>All methods have default no-op implementations so that developers only need to override
 * the lifecycle events they actually care about. Multiple hook implementations can be registered
 * as Spring beans and will all be invoked in order by the generated {@code UserBaseServiceImpl}.
 *
 * <p>Hooks are the correct place to:
 * <ul>
 *   <li>Populate audit fields ({@code createdBy}, {@code updatedBy}) from the security context</li>
 *   <li>Send notifications or events after a write operation</li>
 *   <li>Enforce business invariants before a write operation</li>
 *   <li>Record audit logs</li>
 * </ul>
 *
 * <p>Hooks must NOT perform direct persistence operations — use the repository for that.
 *
 * @param <T>  the domain model type (immutable record)
 * @param <ID> the primary key type
 */
public interface BaseHook<T, ID> {

	/**
	 * Called before a new entity is persisted.
	 *
	 * @param entity the domain model about to be created
	 */
	default void beforeCreate(T entity) {}

	/**
	 * Called after a new entity has been persisted.
	 *
	 * @param entity the saved domain model (includes generated ID and audit timestamps)
	 */
	default void afterCreate(T entity) {}

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
	 * Called before an entity is deleted.
	 *
	 * @param id the primary key of the entity about to be deleted
	 */
	default void beforeDelete(ID id) {}

	/**
	 * Called after an entity has been deleted.
	 *
	 * @param id the primary key of the entity that was deleted
	 */
	default void afterDelete(ID id) {}
}
