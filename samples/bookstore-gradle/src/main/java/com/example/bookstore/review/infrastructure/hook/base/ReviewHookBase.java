package com.example.bookstore.review.infrastructure.hook.base;

import com.example.bookstore.review.domain.model.Review;

/**
 * Lifecycle hook base interface for {@link Review}.
 *
 * <p>All methods have default no-op implementations. Implement this interface as a Spring {@code @Component}
 * (via the generated {@code ReviewHook} class) and override only the lifecycle events you need.
 *
 * <p>Hooks are invoked by {@code ReviewPersistenceAdapterBase} around save and delete operations.
 *
 * <p>This file is always regenerated — do not edit it.
 */
public interface ReviewHookBase {
  /**
   * Called before saving a new or updated entity to the database.
   */
  default void beforeSave(Review entity) {
  }

  /**
   * Called after saving a new or updated entity to the database.
   */
  default void afterSave(Review entity) {
  }

  /**
   * Called before an explicit update operation.
   */
  default void beforeUpdate(Review entity) {
  }

  /**
   * Called after an explicit update operation.
   */
  default void afterUpdate(Review entity) {
  }

  /**
   * Called before deleting (or soft-deleting) an entity.
   */
  default void beforeDelete(Long id) {
  }

  /**
   * Called after deleting (or soft-deleting) an entity.
   */
  default void afterDelete(Long id) {
  }
}
