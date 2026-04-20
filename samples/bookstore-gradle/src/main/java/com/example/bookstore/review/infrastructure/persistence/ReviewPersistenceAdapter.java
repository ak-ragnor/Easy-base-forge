package com.example.bookstore.review.infrastructure.persistence;

import com.example.bookstore.review.domain.entity.ReviewEntity;
import com.example.bookstore.review.domain.model.Review;
import com.example.bookstore.review.infrastructure.hook.base.ReviewHookBase;
import com.example.bookstore.review.infrastructure.persistence.base.ReviewPersistenceAdapterBase;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Developer-owned persistence adapter for {@code Review}.
 *
 * <p>Extend or override methods from {@code ReviewPersistenceAdapterBase} here.
 * Add custom persistence logic as needed.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class ReviewPersistenceAdapter extends ReviewPersistenceAdapterBase {
  public ReviewPersistenceAdapter(ReviewJpaRepository reviewJpaRepository,
      List<ReviewHookBase> reviewHooks) {
    super(reviewJpaRepository, reviewHooks);
  }

  @Override
  protected Review toDomain(ReviewEntity entity) {
    Review domain = new Review();

    domain.setId(entity.getId());
    domain.setRating(entity.getRating());
    domain.setComment(entity.getComment());
    domain.setReviewerName(entity.getReviewerName());
    domain.setBookId(entity.getBookId());

    domain.setCreatedAt(entity.getCreatedAt());
    domain.setCreatedBy(entity.getCreatedBy());
    domain.setUpdatedAt(entity.getUpdatedAt());
    domain.setUpdatedBy(entity.getUpdatedBy());
    domain.setTenantId(entity.getTenantId());

    return domain;
  }

  /**
   * Maps the domain model to a JPA entity.
   * Note: {@code id} is not set — UUID entities self-generate their id.
   * Audit timestamps are managed by Hibernate {@code @CreationTimestamp}/{@code @UpdateTimestamp}.
   */
  @Override
  protected ReviewEntity toEntity(Review domain) {
    ReviewEntity entity = new ReviewEntity();

    entity.setRating(domain.getRating());
    entity.setComment(domain.getComment());
    entity.setReviewerName(domain.getReviewerName());
    entity.setBookId(domain.getBookId());

    return entity;
  }
}
