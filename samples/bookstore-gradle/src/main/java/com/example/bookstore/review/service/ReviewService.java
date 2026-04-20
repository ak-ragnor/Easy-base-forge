package com.example.bookstore.review.service;

import com.example.bookstore.review.service.base.ReviewServiceBaseImpl;
import org.springframework.stereotype.Component;

/**
 * Developer-owned service façade for {@code Review}.
 *
 * <p>Add cross-cutting concerns (security checks, caching, event publishing) here.
 * All CRUD operations are delegated to {@code ReviewLocalServiceBase} via the base class.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class ReviewService extends ReviewServiceBaseImpl {
  public ReviewService(ReviewLocalService reviewLocalService) {
    super(reviewLocalService);
  }
}
