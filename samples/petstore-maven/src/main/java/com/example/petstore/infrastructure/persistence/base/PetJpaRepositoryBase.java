package com.example.petstore.infrastructure.persistence.base;

import com.example.petstore.domain.entity.PetEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Generated Spring Data JPA repository base for {@code PetEntity}.
 *
 * <p>Used internally by {@code PetPersistenceAdapterBase}. Do not inject this interface into service-layer beans.
 *
 * <p>This file is always regenerated — add custom JPQL queries to {@code PetJpaRepository} instead.
 */
@NoRepositoryBean
public interface PetJpaRepositoryBase extends JpaRepository<PetEntity, UUID> {
  @Query("SELECT e FROM PetEntity e WHERE e.id = :id AND e.deleted = false")
  Optional<PetEntity> findActiveById(UUID id);

  @Query("SELECT e FROM PetEntity e WHERE e.deleted = false")
  List<PetEntity> findAllActive();
}
