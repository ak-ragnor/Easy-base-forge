package com.example.bookstore.book.infrastructure.persistence.base;

import com.example.bookstore.book.domain.entity.BookEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Generated Spring Data JPA repository base for {@code BookEntity}.
 *
 * <p>Used internally by {@code BookPersistenceAdapterBase}. Do not inject this interface into service-layer beans.
 *
 * <p>This file is always regenerated — add custom JPQL queries to {@code BookJpaRepository} instead.
 */
@NoRepositoryBean
public interface BookJpaRepositoryBase extends JpaRepository<BookEntity, UUID> {
  @Query("SELECT e FROM BookEntity e WHERE e.id = :id AND e.deleted = false")
  Optional<BookEntity> findActiveById(UUID id);

  @Query("SELECT e FROM BookEntity e WHERE e.deleted = false")
  List<BookEntity> findAllActive();
}
