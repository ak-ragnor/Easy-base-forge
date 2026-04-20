package com.example.bookstore.book.service;

import com.example.bookstore.book.service.base.BookServiceBaseImpl;
import org.springframework.stereotype.Component;

/**
 * Developer-owned service façade for {@code Book}.
 *
 * <p>Add cross-cutting concerns (security checks, caching, event publishing) here.
 * All CRUD operations are delegated to {@code BookLocalServiceBase} via the base class.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class BookService extends BookServiceBaseImpl {
  public BookService(BookLocalService bookLocalService) {
    super(bookLocalService);
  }
}
