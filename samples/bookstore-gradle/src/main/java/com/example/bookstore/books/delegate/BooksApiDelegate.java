package com.example.bookstore.books.delegate;

import com.example.bookstore.books.dto.BookDTO;
import com.example.bookstore.books.dto.CreateBookRequest;
import com.example.bookstore.books.dto.UpdateBookRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

/**
 * @generated
 */
public interface BooksApiDelegate {
    Page<BookDTO> listBooks(UUID authorId, String genre, Pageable pageable);

    BookDTO createBook(CreateBookRequest createBookRequest);

    BookDTO getBookById(UUID id);

    BookDTO updateBook(UUID id, UpdateBookRequest updateBookRequest);

    ResponseEntity<Void> deleteBook(UUID id);
}
