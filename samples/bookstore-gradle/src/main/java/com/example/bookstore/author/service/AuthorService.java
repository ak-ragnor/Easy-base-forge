package com.example.bookstore.author.service;

import com.example.bookstore.author.service.base.AuthorServiceBaseImpl;
import org.springframework.stereotype.Component;

/**
 * Developer-owned service façade for {@code Author}.
 *
 * <p>Add cross-cutting concerns (security checks, caching, event publishing) here.
 * All CRUD operations are delegated to {@code AuthorLocalServiceBase} via the base class.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class AuthorService extends AuthorServiceBaseImpl {
  public AuthorService(AuthorLocalService authorLocalService) {
    super(authorLocalService);
  }
}
