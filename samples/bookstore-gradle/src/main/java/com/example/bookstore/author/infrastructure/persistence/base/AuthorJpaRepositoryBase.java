package com.example.bookstore.author.infrastructure.persistence.base;

import com.example.bookstore.author.domain.entity.AuthorEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Generated Spring Data JPA repository base for {@code AuthorEntity}.
 *
 * <p>Used internally by {@code AuthorPersistenceAdapterBase}. Do not inject this interface into service-layer beans.
 *
 * <p>This file is always regenerated — add custom JPQL queries to {@code AuthorJpaRepository} instead.
 */
@NoRepositoryBean
public interface AuthorJpaRepositoryBase extends JpaRepository<AuthorEntity, UUID> {
  @Query("SELECT e FROM AuthorEntity e WHERE e.id = :id AND e.deleted = false")
  Optional<AuthorEntity> findActiveById(UUID id);

  @Query("SELECT e FROM AuthorEntity e WHERE e.deleted = false")
  List<AuthorEntity> findAllActive();
}
