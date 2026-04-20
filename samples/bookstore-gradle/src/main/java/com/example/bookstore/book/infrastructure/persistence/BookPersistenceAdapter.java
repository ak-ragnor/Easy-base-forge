package com.example.bookstore.book.infrastructure.persistence;

import com.example.bookstore.book.domain.entity.BookEntity;
import com.example.bookstore.book.domain.model.Book;
import com.example.bookstore.book.infrastructure.hook.base.BookHookBase;
import com.example.bookstore.book.infrastructure.persistence.base.BookPersistenceAdapterBase;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Developer-owned persistence adapter for {@code Book}.
 *
 * <p>Extend or override methods from {@code BookPersistenceAdapterBase} here.
 * Add custom persistence logic as needed.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class BookPersistenceAdapter extends BookPersistenceAdapterBase {
  public BookPersistenceAdapter(BookJpaRepository bookJpaRepository, List<BookHookBase> bookHooks) {
    super(bookJpaRepository, bookHooks);
  }

  @Override
  protected Book toDomain(BookEntity entity) {
    Book domain = new Book();

    domain.setId(entity.getId());
    domain.setTitle(entity.getTitle());
    domain.setIsbn(entity.getIsbn());
    domain.setGenre(entity.getGenre());
    domain.setPrice(entity.getPrice());
    domain.setPublishedDate(entity.getPublishedDate());
    domain.setAuthorId(entity.getAuthorId());
    domain.setCoverImageId(entity.getCoverImageId());

    domain.setCreatedAt(entity.getCreatedAt());
    domain.setCreatedBy(entity.getCreatedBy());
    domain.setUpdatedAt(entity.getUpdatedAt());
    domain.setUpdatedBy(entity.getUpdatedBy());
    domain.setDeleted(entity.getDeleted());
    domain.setTenantId(entity.getTenantId());

    return domain;
  }

  /**
   * Maps the domain model to a JPA entity.
   * Note: {@code id} is not set — UUID entities self-generate their id.
   * Audit timestamps are managed by Hibernate {@code @CreationTimestamp}/{@code @UpdateTimestamp}.
   */
  @Override
  protected BookEntity toEntity(Book domain) {
    BookEntity entity = new BookEntity();

    entity.setTitle(domain.getTitle());
    entity.setIsbn(domain.getIsbn());
    entity.setGenre(domain.getGenre());
    entity.setPrice(domain.getPrice());
    entity.setPublishedDate(domain.getPublishedDate());
    entity.setAuthorId(domain.getAuthorId());
    entity.setCoverImageId(domain.getCoverImageId());

    return entity;
  }
}
