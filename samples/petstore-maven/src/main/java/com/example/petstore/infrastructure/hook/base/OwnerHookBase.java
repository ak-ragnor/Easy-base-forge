package com.example.petstore.infrastructure.hook.base;

import com.example.petstore.domain.model.Owner;
import java.util.UUID;

/**
 * Lifecycle hook base interface for {@link Owner}.
 *
 * <p>All methods have default no-op implementations. Implement this interface as a Spring {@code @Component}
 * (via the generated {@code OwnerHook} class) and override only the lifecycle events you need.
 *
 * <p>Hooks are invoked by {@code OwnerPersistenceAdapterBase} around save and delete operations.
 *
 * <p>This file is always regenerated — do not edit it.
 */
public interface OwnerHookBase {
  /**
   * Called before saving a new or updated entity to the database.
   */
  default void beforeSave(Owner entity) {
  }

  /**
   * Called after saving a new or updated entity to the database.
   */
  default void afterSave(Owner entity) {
  }

  /**
   * Called before an explicit update operation.
   */
  default void beforeUpdate(Owner entity) {
  }

  /**
   * Called after an explicit update operation.
   */
  default void afterUpdate(Owner entity) {
  }

  /**
   * Called before deleting (or soft-deleting) an entity.
   */
  default void beforeDelete(UUID id) {
  }

  /**
   * Called after deleting (or soft-deleting) an entity.
   */
  default void afterDelete(UUID id) {
  }
}
