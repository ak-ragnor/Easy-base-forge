package com.example.bookstore.books.delegate.impl;

import com.example.bookstore.book.domain.model.Book;
import com.example.bookstore.book.service.BookService;
import com.example.bookstore.books.delegate.impl.base.BooksApiDelegateImplBase;
import com.example.bookstore.books.dto.BookDTO;
import com.example.bookstore.books.dto.CreateBookRequest;
import com.example.bookstore.books.dto.UpdateBookRequest;
import java.time.OffsetDateTime;
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
public class BooksApiDelegateImpl extends BooksApiDelegateImplBase {

    private final BookService bookService;

    public BooksApiDelegateImpl(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public Page<BookDTO> listBooks(UUID authorId, String genre, Pageable pageable) {
        List<BookDTO> all = bookService.findAll().stream()
                .filter(b -> authorId == null || authorId.equals(b.getAuthorId()))
                .filter(b -> genre == null || genre.equalsIgnoreCase(b.getGenre()))
                .map(this::toDTO)
                .collect(Collectors.toList());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<BookDTO> page = start > all.size() ? List.of() : all.subList(start, end);
        return new PageImpl<>(page, pageable, all.size());
    }

    @Override
    public BookDTO createBook(CreateBookRequest req) {
        Book book = new Book();
        book.setTitle(req.getTitle());
        book.setIsbn(req.getIsbn());
        book.setGenre(req.getGenre());
        book.setPrice(req.getPrice());
        book.setPublishedDate(req.getPublishedDate());
        book.setAuthorId(req.getAuthorId());
        book.setCoverImageId(req.getCoverImageId());
        return toDTO(bookService.create(book));
    }

    @Override
    public BookDTO getBookById(UUID id) {
        return bookService.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Override
    public BookDTO updateBook(UUID id, UpdateBookRequest req) {
        Book existing = bookService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (req.getTitle() != null) existing.setTitle(req.getTitle());
        if (req.getGenre() != null) existing.setGenre(req.getGenre());
        if (req.getPrice() != null) existing.setPrice(req.getPrice());
        if (req.getCoverImageId() != null) existing.setCoverImageId(req.getCoverImageId());
        return toDTO(bookService.update(id, existing));
    }

    @Override
    public ResponseEntity<Void> deleteBook(UUID id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private BookDTO toDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setIsbn(book.getIsbn());
        dto.setGenre(book.getGenre());
        dto.setPrice(book.getPrice());
        dto.setPublishedDate(book.getPublishedDate());
        dto.setAuthorId(book.getAuthorId());
        dto.setCoverImageId(book.getCoverImageId());
        if (book.getCreatedAt() != null) {
            dto.setCreatedAt(book.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        return dto;
    }
}
