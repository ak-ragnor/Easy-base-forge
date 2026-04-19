package com.example.petstore.infrastructure.repository.base;

import com.easybase.forge.service.BaseRepository;
import com.example.petstore.domain.model.Pet;
import java.util.UUID;

/**
 * Generated base repository interface for {@link Pet}.
 *
 * <p>The service layer depends only on this contract. The persistence implementation is in {@code PetPersistenceAdapter}.
 *
 * <p>This file is always regenerated — add custom query methods to {@code PetRepository} instead.
 */
public interface PetRepositoryBase extends BaseRepository<Pet, UUID> {
}
