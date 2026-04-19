package com.example.petstore.infrastructure.persistence.base;

import com.example.petstore.domain.entity.OwnerEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Generated Spring Data JPA repository base for {@code OwnerEntity}.
 *
 * <p>Used internally by {@code OwnerPersistenceAdapterBase}. Do not inject this interface into service-layer beans.
 *
 * <p>This file is always regenerated — add custom JPQL queries to {@code OwnerJpaRepository} instead.
 */
@NoRepositoryBean
public interface OwnerJpaRepositoryBase extends JpaRepository<OwnerEntity, UUID> {
  @Query("SELECT e FROM OwnerEntity e WHERE e.id = :id AND e.deleted = false")
  Optional<OwnerEntity> findActiveById(UUID id);

  @Query("SELECT e FROM OwnerEntity e WHERE e.deleted = false")
  List<OwnerEntity> findAllActive();
}
