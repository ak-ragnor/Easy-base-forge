package com.example.bookstore.review.infrastructure.persistence.base;

import com.example.bookstore.review.domain.entity.ReviewEntity;
import com.example.bookstore.review.domain.model.Review;
import com.example.bookstore.review.infrastructure.hook.base.ReviewHookBase;
import com.example.bookstore.review.infrastructure.persistence.ReviewJpaRepository;
import com.example.bookstore.review.infrastructure.repository.ReviewRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Abstract persistence adapter base for {@code Review}.
 *
 * <p>Bridges {@link ReviewRepository} and Spring Data JPA. Implement {@code toDomain} and {@code toEntity} in {@code ReviewPersistenceAdapter}.
 *
 * <p>This file is always regenerated — do not edit it.
 */
public abstract class ReviewPersistenceAdapterBase implements ReviewRepository {
  protected final ReviewJpaRepository reviewJpaRepository;

  protected final List<ReviewHookBase> reviewHooks;

  protected ReviewPersistenceAdapterBase(ReviewJpaRepository jpaRepo, List<ReviewHookBase> hooks) {
    this.reviewJpaRepository = jpaRepo;
    this.reviewHooks = hooks != null ? hooks : Collections.emptyList();
  }

  @Override
  public Review create(Review domain) {
    for (ReviewHookBase h : reviewHooks) {
      h.beforeSave(domain);
    }

    ReviewEntity entity = toEntity(domain);

    Review saved = toDomain(reviewJpaRepository.save(entity));

    for (ReviewHookBase h : reviewHooks) {
      h.afterSave(saved);
    }

    return saved;
  }

  @Override
  public Review update(Long id, Review domain) {
    for (ReviewHookBase h : reviewHooks) {
      h.beforeUpdate(domain);
    }

    ReviewEntity entity = toEntity(domain);

    Review saved = toDomain(reviewJpaRepository.save(entity));

    for (ReviewHookBase h : reviewHooks) {
      h.afterUpdate(saved);
    }

    return saved;
  }

  @Override
  public Optional<Review> findById(Long id) {
    return reviewJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public List<Review> findAll() {
    List<ReviewEntity> entities = reviewJpaRepository.findAll();

    List<Review> result = new ArrayList<>();

    for (ReviewEntity e : entities) {
      result.add(toDomain(e));
    }

    return result;
  }

  @Override
  public void deleteById(Long id) {
    for (ReviewHookBase h : reviewHooks) {
      h.beforeDelete(id);
    }

    reviewJpaRepository.deleteById(id);

    for (ReviewHookBase h : reviewHooks) {
      h.afterDelete(id);
    }
  }

  @Override
  public boolean existsById(Long id) {
    return reviewJpaRepository.existsById(id);
  }

  /**
   * Maps a JPA {@code ReviewEntity} to the domain model {@link Review}.
   */
  protected abstract Review toDomain(ReviewEntity entity);

  /**
   * Maps the domain model {@link Review} to a JPA {@code ReviewEntity}.
   */
  protected abstract ReviewEntity toEntity(Review domain);
}
