package com.example.petstore.infrastructure.repository.base;

import com.easybase.forge.service.BaseRepository;
import com.example.petstore.domain.model.Owner;
import java.util.UUID;

/**
 * Generated base repository interface for {@link Owner}.
 *
 * <p>The service layer depends only on this contract. The persistence implementation is in {@code OwnerPersistenceAdapter}.
 *
 * <p>This file is always regenerated — add custom query methods to {@code OwnerRepository} instead.
 */
public interface OwnerRepositoryBase extends BaseRepository<Owner, UUID> {
}
