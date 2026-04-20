package com.example.bookstore.book.infrastructure.persistence.base;

import com.example.bookstore.book.domain.entity.BookEntity;
import com.example.bookstore.book.domain.model.Book;
import com.example.bookstore.book.infrastructure.hook.base.BookHookBase;
import com.example.bookstore.book.infrastructure.persistence.BookJpaRepository;
import com.example.bookstore.book.infrastructure.repository.BookRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Abstract persistence adapter base for {@code Book}.
 *
 * <p>Bridges {@link BookRepository} and Spring Data JPA. Implement {@code toDomain} and {@code toEntity} in {@code BookPersistenceAdapter}.
 *
 * <p>This file is always regenerated — do not edit it.
 */
public abstract class BookPersistenceAdapterBase implements BookRepository {
  protected final BookJpaRepository bookJpaRepository;

  protected final List<BookHookBase> bookHooks;

  protected BookPersistenceAdapterBase(BookJpaRepository jpaRepo, List<BookHookBase> hooks) {
    this.bookJpaRepository = jpaRepo;
    this.bookHooks = hooks != null ? hooks : Collections.emptyList();
  }

  @Override
  public Book create(Book domain) {
    for (BookHookBase h : bookHooks) {
      h.beforeSave(domain);
    }

    BookEntity entity = toEntity(domain);

    Book saved = toDomain(bookJpaRepository.save(entity));

    for (BookHookBase h : bookHooks) {
      h.afterSave(saved);
    }

    return saved;
  }

  @Override
  public Book update(UUID id, Book domain) {
    for (BookHookBase h : bookHooks) {
      h.beforeUpdate(domain);
    }

    BookEntity entity = toEntity(domain);

    Book saved = toDomain(bookJpaRepository.save(entity));

    for (BookHookBase h : bookHooks) {
      h.afterUpdate(saved);
    }

    return saved;
  }

  @Override
  public Optional<Book> findById(UUID id) {
    return bookJpaRepository.findActiveById(id).map(this::toDomain);
  }

  @Override
  public List<Book> findAll() {
    List<BookEntity> entities = bookJpaRepository.findAllActive();

    List<Book> result = new ArrayList<>();

    for (BookEntity e : entities) {
      result.add(toDomain(e));
    }

    return result;
  }

  @Override
  public void deleteById(UUID id) {
    for (BookHookBase h : bookHooks) {
      h.beforeDelete(id);
    }

    BookEntity entity = bookJpaRepository.findById(id).orElse(null);

    if (entity != null) {
      entity.setDeleted(true);

      bookJpaRepository.save(entity);
    }

    for (BookHookBase h : bookHooks) {
      h.afterDelete(id);
    }
  }

  @Override
  public boolean existsById(UUID id) {
    return bookJpaRepository.existsById(id);
  }

  /**
   * Maps a JPA {@code BookEntity} to the domain model {@link Book}.
   */
  protected abstract Book toDomain(BookEntity entity);

  /**
   * Maps the domain model {@link Book} to a JPA {@code BookEntity}.
   */
  protected abstract BookEntity toEntity(Book domain);
}
