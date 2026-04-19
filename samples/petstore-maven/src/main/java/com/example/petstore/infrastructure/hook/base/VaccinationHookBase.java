package com.example.petstore.infrastructure.hook.base;

import com.example.petstore.domain.model.Vaccination;
import java.util.UUID;

/**
 * Lifecycle hook base interface for {@link Vaccination}.
 *
 * <p>All methods have default no-op implementations. Implement this interface as a Spring {@code @Component}
 * (via the generated {@code VaccinationHook} class) and override only the lifecycle events you need.
 *
 * <p>Hooks are invoked by {@code VaccinationPersistenceAdapterBase} around save and delete operations.
 *
 * <p>This file is always regenerated — do not edit it.
 */
public interface VaccinationHookBase {
  /**
   * Called before saving a new or updated entity to the database.
   */
  default void beforeSave(Vaccination entity) {
  }

  /**
   * Called after saving a new or updated entity to the database.
   */
  default void afterSave(Vaccination entity) {
  }

  /**
   * Called before an explicit update operation.
   */
  default void beforeUpdate(Vaccination entity) {
  }

  /**
   * Called after an explicit update operation.
   */
  default void afterUpdate(Vaccination entity) {
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
