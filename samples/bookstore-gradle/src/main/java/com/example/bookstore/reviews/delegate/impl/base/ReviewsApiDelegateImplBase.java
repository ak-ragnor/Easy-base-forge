package com.example.bookstore.reviews.delegate.impl.base;

import com.example.bookstore.reviews.delegate.ReviewsApiDelegate;
import com.example.bookstore.reviews.dto.CreateReviewRequest;
import com.example.bookstore.reviews.dto.ReviewDTO;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public abstract class ReviewsApiDelegateImplBase implements ReviewsApiDelegate {
    @Override
    public Page<ReviewDTO> listReviews(UUID bookId, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ReviewDTO createReview(CreateReviewRequest createReviewRequest) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ReviewDTO getReviewById(Long id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResponseEntity<Void> deleteReview(Long id) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
