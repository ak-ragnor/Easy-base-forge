package com.example.bookstore.author.infrastructure.repository.base;

import com.easybase.forge.service.BaseRepository;
import com.example.bookstore.author.domain.model.Author;
import java.util.UUID;

/**
 * Generated base repository interface for {@link Author}.
 *
 * <p>The service layer depends only on this contract. The persistence implementation is in {@code AuthorPersistenceAdapter}.
 *
 * <p>This file is always regenerated — add custom query methods to {@code AuthorRepository} instead.
 */
public interface AuthorRepositoryBase extends BaseRepository<Author, UUID> {
}
