package com.example.bookstore.review.infrastructure.repository.base;

import com.easybase.forge.service.BaseRepository;
import com.example.bookstore.review.domain.model.Review;

/**
 * Generated base repository interface for {@link Review}.
 *
 * <p>The service layer depends only on this contract. The persistence implementation is in {@code ReviewPersistenceAdapter}.
 *
 * <p>This file is always regenerated — add custom query methods to {@code ReviewRepository} instead.
 */
public interface ReviewRepositoryBase extends BaseRepository<Review, Long> {
}
