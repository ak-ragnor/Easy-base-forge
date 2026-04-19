package com.example.bookstore.reviews.controller;

import com.example.bookstore.reviews.controller.base.ReviewsControllerBase;
import com.example.bookstore.reviews.delegate.ReviewsApiDelegate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewsController extends ReviewsControllerBase {
    public ReviewsController(ReviewsApiDelegate delegate) {
        super(delegate);
    }
}
