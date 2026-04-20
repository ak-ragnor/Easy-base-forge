package com.example.bookstore.review.infrastructure.persistence.base;

import com.example.bookstore.review.domain.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Generated Spring Data JPA repository base for {@code ReviewEntity}.
 *
 * <p>Used internally by {@code ReviewPersistenceAdapterBase}. Do not inject this interface into service-layer beans.
 *
 * <p>This file is always regenerated — add custom JPQL queries to {@code ReviewJpaRepository} instead.
 */
@NoRepositoryBean
public interface ReviewJpaRepositoryBase extends JpaRepository<ReviewEntity, Long> {
}
