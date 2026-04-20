package com.example.bookstore.book.infrastructure.hook.base;

import com.example.bookstore.book.domain.model.Book;
import java.util.UUID;

/**
 * Lifecycle hook base interface for {@link Book}.
 *
 * <p>All methods have default no-op implementations. Implement this interface as a Spring {@code @Component}
 * (via the generated {@code BookHook} class) and override only the lifecycle events you need.
 *
 * <p>Hooks are invoked by {@code BookPersistenceAdapterBase} around save and delete operations.
 *
 * <p>This file is always regenerated — do not edit it.
 */
public interface BookHookBase {
  /**
   * Called before saving a new or updated entity to the database.
   */
  default void beforeSave(Book entity) {
  }

  /**
   * Called after saving a new or updated entity to the database.
   */
  default void afterSave(Book entity) {
  }

  /**
   * Called before an explicit update operation.
   */
  default void beforeUpdate(Book entity) {
  }

  /**
   * Called after an explicit update operation.
   */
  default void afterUpdate(Book entity) {
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
