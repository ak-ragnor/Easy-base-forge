package com.example.bookstore.author.infrastructure.persistence;

import com.example.bookstore.author.domain.entity.AuthorEntity;
import com.example.bookstore.author.domain.model.Author;
import com.example.bookstore.author.infrastructure.hook.base.AuthorHookBase;
import com.example.bookstore.author.infrastructure.persistence.base.AuthorPersistenceAdapterBase;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Developer-owned persistence adapter for {@code Author}.
 *
 * <p>Extend or override methods from {@code AuthorPersistenceAdapterBase} here.
 * Add custom persistence logic as needed.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class AuthorPersistenceAdapter extends AuthorPersistenceAdapterBase {
  public AuthorPersistenceAdapter(AuthorJpaRepository authorJpaRepository,
      List<AuthorHookBase> authorHooks) {
    super(authorJpaRepository, authorHooks);
  }

  @Override
  protected Author toDomain(AuthorEntity entity) {
    Author domain = new Author();

    domain.setId(entity.getId());
    domain.setName(entity.getName());
    domain.setBio(entity.getBio());
    domain.setEmail(entity.getEmail());

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
  protected AuthorEntity toEntity(Author domain) {
    AuthorEntity entity = new AuthorEntity();

    entity.setName(domain.getName());
    entity.setBio(domain.getBio());
    entity.setEmail(domain.getEmail());

    return entity;
  }
}
