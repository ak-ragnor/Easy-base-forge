package com.example.bookstore.authors.controller;

import com.example.bookstore.authors.controller.base.AuthorsControllerBase;
import com.example.bookstore.authors.delegate.AuthorsApiDelegate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorsController extends AuthorsControllerBase {
    public AuthorsController(AuthorsApiDelegate delegate) {
        super(delegate);
    }
}
