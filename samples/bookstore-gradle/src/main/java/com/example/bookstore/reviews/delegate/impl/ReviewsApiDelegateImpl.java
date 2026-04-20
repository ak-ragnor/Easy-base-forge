package com.example.bookstore.reviews.delegate.impl;

import com.example.bookstore.review.domain.model.Review;
import com.example.bookstore.review.service.ReviewService;
import com.example.bookstore.reviews.delegate.impl.base.ReviewsApiDelegateImplBase;
import com.example.bookstore.reviews.dto.CreateReviewRequest;
import com.example.bookstore.reviews.dto.ReviewDTO;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ReviewsApiDelegateImpl extends ReviewsApiDelegateImplBase {

    private final ReviewService reviewService;

    public ReviewsApiDelegateImpl(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Override
    public Page<ReviewDTO> listReviews(UUID bookId, Pageable pageable) {
        List<ReviewDTO> all = reviewService.findAll().stream()
                .filter(r -> bookId == null || bookId.equals(r.getBookId()))
                .map(this::toDTO)
                .collect(Collectors.toList());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<ReviewDTO> page = start > all.size() ? List.of() : all.subList(start, end);
        return new PageImpl<>(page, pageable, all.size());
    }

    @Override
    public ReviewDTO createReview(CreateReviewRequest req) {
        Review review = new Review();
        review.setRating(req.getRating());
        review.setComment(req.getComment());
        review.setReviewerName(req.getReviewerName());
        review.setBookId(req.getBookId());
        return toDTO(reviewService.create(review));
    }

    @Override
    public ReviewDTO getReviewById(Long id) {
        return reviewService.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Override
    public ResponseEntity<Void> deleteReview(Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ReviewDTO toDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setReviewerName(review.getReviewerName());
        dto.setBookId(review.getBookId());
        if (review.getCreatedAt() != null) {
            dto.setCreatedAt(review.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        return dto;
    }
}
