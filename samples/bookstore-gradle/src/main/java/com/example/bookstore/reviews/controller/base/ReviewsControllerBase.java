package com.example.bookstore.reviews.controller.base;

import com.example.bookstore.reviews.delegate.ReviewsApiDelegate;
import com.example.bookstore.reviews.dto.CreateReviewRequest;
import com.example.bookstore.reviews.dto.ReviewDTO;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @generated
 */
public abstract class ReviewsControllerBase {
    private final ReviewsApiDelegate delegate;

    protected ReviewsControllerBase(ReviewsApiDelegate delegate) {
        this.delegate = delegate;
    }

    protected ReviewsApiDelegate getDelegate() {
        return delegate;
    }

    /**
     * <pre>curl -X GET http://localhost:8080/reviews</pre>
     */
    @GetMapping("/reviews")
    public Page<ReviewDTO> listReviews(
            @RequestParam(value = "bookId", required = false) UUID bookId, Pageable pageable) {
        return delegate.listReviews(bookId, pageable);
    }

    /**
     * <pre>curl -X POST http://localhost:8080/reviews \
     *   -H 'Content-Type: application/json' \
     *   -d '{}'</pre>
     */
    @PostMapping("/reviews")
    public ReviewDTO createReview(
            @Valid @RequestBody(required = true) CreateReviewRequest createReviewRequest) {
        return delegate.createReview(createReviewRequest);
    }

    /**
     * <pre>curl -X GET http://localhost:8080/reviews/{id}</pre>
     */
    @GetMapping("/reviews/{id}")
    public ReviewDTO getReviewById(@PathVariable("id") Long id) {
        return delegate.getReviewById(id);
    }

    /**
     * <pre>curl -X DELETE http://localhost:8080/reviews/{id}</pre>
     */
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable("id") Long id) {
        return delegate.deleteReview(id);
    }
}
