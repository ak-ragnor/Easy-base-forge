package com.example.bookstore.book.service;

import com.example.bookstore.book.infrastructure.repository.BookRepository;
import com.example.bookstore.book.service.base.BookLocalServiceBaseImpl;
import org.springframework.stereotype.Service;

/**
 * Developer-owned local service for {@code Book}.
 *
 * <p>Add business logic and domain-specific operations here.
 * CRUD operations are inherited from {@code BookLocalServiceBaseImpl}.
 *
 * <p>This file is generated once and never overwritten.
 */
@Service
public class BookLocalService extends BookLocalServiceBaseImpl {
  public BookLocalService(BookRepository bookRepository) {
    super(bookRepository);
  }
}
