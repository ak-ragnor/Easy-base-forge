package com.example.bookstore.review.service;

import com.example.bookstore.review.infrastructure.repository.ReviewRepository;
import com.example.bookstore.review.service.base.ReviewLocalServiceBaseImpl;
import org.springframework.stereotype.Service;

/**
 * Developer-owned local service for {@code Review}.
 *
 * <p>Add business logic and domain-specific operations here.
 * CRUD operations are inherited from {@code ReviewLocalServiceBaseImpl}.
 *
 * <p>This file is generated once and never overwritten.
 */
@Service
public class ReviewLocalService extends ReviewLocalServiceBaseImpl {
  public ReviewLocalService(ReviewRepository reviewRepository) {
    super(reviewRepository);
  }
}
