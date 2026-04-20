package com.example.bookstore.book.infrastructure.repository.base;

import com.easybase.forge.service.BaseRepository;
import com.example.bookstore.book.domain.model.Book;
import java.util.UUID;

/**
 * Generated base repository interface for {@link Book}.
 *
 * <p>The service layer depends only on this contract. The persistence implementation is in {@code BookPersistenceAdapter}.
 *
 * <p>This file is always regenerated — add custom query methods to {@code BookRepository} instead.
 */
public interface BookRepositoryBase extends BaseRepository<Book, UUID> {
}
