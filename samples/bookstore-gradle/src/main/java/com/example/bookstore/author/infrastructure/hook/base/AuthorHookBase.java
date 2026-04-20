package com.example.bookstore.author.infrastructure.hook.base;

import com.example.bookstore.author.domain.model.Author;
import java.util.UUID;

/**
 * Lifecycle hook base interface for {@link Author}.
 *
 * <p>All methods have default no-op implementations. Implement this interface as a Spring {@code @Component}
 * (via the generated {@code AuthorHook} class) and override only the lifecycle events you need.
 *
 * <p>Hooks are invoked by {@code AuthorPersistenceAdapterBase} around save and delete operations.
 *
 * <p>This file is always regenerated — do not edit it.
 */
public interface AuthorHookBase {
  /**
   * Called before saving a new or updated entity to the database.
   */
  default void beforeSave(Author entity) {
  }

  /**
   * Called after saving a new or updated entity to the database.
   */
  default void afterSave(Author entity) {
  }

  /**
   * Called before an explicit update operation.
   */
  default void beforeUpdate(Author entity) {
  }

  /**
   * Called after an explicit update operation.
   */
  default void afterUpdate(Author entity) {
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
