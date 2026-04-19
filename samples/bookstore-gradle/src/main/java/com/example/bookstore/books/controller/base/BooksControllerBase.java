package com.example.bookstore.books.controller.base;

import com.example.bookstore.books.delegate.BooksApiDelegate;
import com.example.bookstore.books.dto.BookDTO;
import com.example.bookstore.books.dto.CreateBookRequest;
import com.example.bookstore.books.dto.UpdateBookRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @generated
 */
public abstract class BooksControllerBase {
    private final BooksApiDelegate delegate;

    protected BooksControllerBase(BooksApiDelegate delegate) {
        this.delegate = delegate;
    }

    protected BooksApiDelegate getDelegate() {
        return delegate;
    }

    /**
     * <pre>curl -X GET http://localhost:8080/books</pre>
     */
    @GetMapping("/books")
    public Page<BookDTO> listBooks(
            @RequestParam(value = "authorId", required = false) UUID authorId,
            @RequestParam(value = "genre", required = false) String genre, Pageable pageable) {
        return delegate.listBooks(authorId, genre, pageable);
    }

    /**
     * <pre>curl -X POST http://localhost:8080/books \
     *   -H 'Content-Type: application/json' \
     *   -d '{}'</pre>
     */
    @PostMapping("/books")
    public BookDTO createBook(
            @Valid @RequestBody(required = true) CreateBookRequest createBookRequest) {
        return delegate.createBook(createBookRequest);
    }

    /**
     * <pre>curl -X GET http://localhost:8080/books/{id}</pre>
     */
    @GetMapping("/books/{id}")
    public BookDTO getBookById(@PathVariable("id") UUID id) {
        return delegate.getBookById(id);
    }

    /**
     * <pre>curl -X PUT http://localhost:8080/books/{id} \
     *   -H 'Content-Type: application/json' \
     *   -d '{}'</pre>
     */
    @PutMapping("/books/{id}")
    public BookDTO updateBook(@PathVariable("id") UUID id,
            @Valid @RequestBody(required = true) UpdateBookRequest updateBookRequest) {
        return delegate.updateBook(id, updateBookRequest);
    }

    /**
     * <pre>curl -X DELETE http://localhost:8080/books/{id}</pre>
     */
    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable("id") UUID id) {
        return delegate.deleteBook(id);
    }
}
