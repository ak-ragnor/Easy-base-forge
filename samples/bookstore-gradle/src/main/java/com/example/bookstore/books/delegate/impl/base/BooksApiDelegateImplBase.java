package com.example.bookstore.books.delegate.impl.base;

import com.example.bookstore.books.delegate.BooksApiDelegate;
import com.example.bookstore.books.dto.BookDTO;
import com.example.bookstore.books.dto.CreateBookRequest;
import com.example.bookstore.books.dto.UpdateBookRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public abstract class BooksApiDelegateImplBase implements BooksApiDelegate {
    @Override
    public Page<BookDTO> listBooks(UUID authorId, String genre, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BookDTO createBook(CreateBookRequest createBookRequest) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BookDTO getBookById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BookDTO updateBook(UUID id, UpdateBookRequest updateBookRequest) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResponseEntity<Void> deleteBook(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
