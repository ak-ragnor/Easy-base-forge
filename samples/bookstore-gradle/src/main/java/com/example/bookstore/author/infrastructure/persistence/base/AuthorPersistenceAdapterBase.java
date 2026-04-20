package com.example.bookstore.author.infrastructure.persistence.base;

import com.example.bookstore.author.domain.entity.AuthorEntity;
import com.example.bookstore.author.domain.model.Author;
import com.example.bookstore.author.infrastructure.hook.base.AuthorHookBase;
import com.example.bookstore.author.infrastructure.persistence.AuthorJpaRepository;
import com.example.bookstore.author.infrastructure.repository.AuthorRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Abstract persistence adapter base for {@code Author}.
 *
 * <p>Bridges {@link AuthorRepository} and Spring Data JPA. Implement {@code toDomain} and {@code toEntity} in {@code AuthorPersistenceAdapter}.
 *
 * <p>This file is always regenerated — do not edit it.
 */
public abstract class AuthorPersistenceAdapterBase implements AuthorRepository {
  protected final AuthorJpaRepository authorJpaRepository;

  protected final List<AuthorHookBase> authorHooks;

  protected AuthorPersistenceAdapterBase(AuthorJpaRepository jpaRepo, List<AuthorHookBase> hooks) {
    this.authorJpaRepository = jpaRepo;
    this.authorHooks = hooks != null ? hooks : Collections.emptyList();
  }

  @Override
  public Author create(Author domain) {
    for (AuthorHookBase h : authorHooks) {
      h.beforeSave(domain);
    }

    AuthorEntity entity = toEntity(domain);

    Author saved = toDomain(authorJpaRepository.save(entity));

    for (AuthorHookBase h : authorHooks) {
      h.afterSave(saved);
    }

    return saved;
  }

  @Override
  public Author update(UUID id, Author domain) {
    for (AuthorHookBase h : authorHooks) {
      h.beforeUpdate(domain);
    }

    AuthorEntity entity = toEntity(domain);

    Author saved = toDomain(authorJpaRepository.save(entity));

    for (AuthorHookBase h : authorHooks) {
      h.afterUpdate(saved);
    }

    return saved;
  }

  @Override
  public Optional<Author> findById(UUID id) {
    return authorJpaRepository.findActiveById(id).map(this::toDomain);
  }

  @Override
  public List<Author> findAll() {
    List<AuthorEntity> entities = authorJpaRepository.findAllActive();

    List<Author> result = new ArrayList<>();

    for (AuthorEntity e : entities) {
      result.add(toDomain(e));
    }

    return result;
  }

  @Override
  public void deleteById(UUID id) {
    for (AuthorHookBase h : authorHooks) {
      h.beforeDelete(id);
    }

    AuthorEntity entity = authorJpaRepository.findById(id).orElse(null);

    if (entity != null) {
      entity.setDeleted(true);

      authorJpaRepository.save(entity);
    }

    for (AuthorHookBase h : authorHooks) {
      h.afterDelete(id);
    }
  }

  @Override
  public boolean existsById(UUID id) {
    return authorJpaRepository.existsById(id);
  }

  /**
   * Maps a JPA {@code AuthorEntity} to the domain model {@link Author}.
   */
  protected abstract Author toDomain(AuthorEntity entity);

  /**
   * Maps the domain model {@link Author} to a JPA {@code AuthorEntity}.
   */
  protected abstract AuthorEntity toEntity(Author domain);
}
