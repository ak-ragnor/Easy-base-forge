package com.example.bookstore.author.service;

import com.example.bookstore.author.infrastructure.repository.AuthorRepository;
import com.example.bookstore.author.service.base.AuthorLocalServiceBaseImpl;
import org.springframework.stereotype.Service;

/**
 * Developer-owned local service for {@code Author}.
 *
 * <p>Add business logic and domain-specific operations here.
 * CRUD operations are inherited from {@code AuthorLocalServiceBaseImpl}.
 *
 * <p>This file is generated once and never overwritten.
 */
@Service
public class AuthorLocalService extends AuthorLocalServiceBaseImpl {
  public AuthorLocalService(AuthorRepository authorRepository) {
    super(authorRepository);
  }
}
