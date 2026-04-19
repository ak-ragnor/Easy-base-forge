package com.example.bookstore.reviews.delegate;

import com.example.bookstore.reviews.dto.CreateReviewRequest;
import com.example.bookstore.reviews.dto.ReviewDTO;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

/**
 * @generated
 */
public interface ReviewsApiDelegate {
    Page<ReviewDTO> listReviews(UUID bookId, Pageable pageable);

    ReviewDTO createReview(CreateReviewRequest createReviewRequest);

    ReviewDTO getReviewById(Long id);

    ResponseEntity<Void> deleteReview(Long id);
}
