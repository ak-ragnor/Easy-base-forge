package com.example.bookstore.books.controller;

import com.example.bookstore.books.controller.base.BooksControllerBase;
import com.example.bookstore.books.delegate.BooksApiDelegate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BooksController extends BooksControllerBase {
    public BooksController(BooksApiDelegate delegate) {
        super(delegate);
    }
}
