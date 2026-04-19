package com.example.petstore.infrastructure.repository.base;

import com.easybase.forge.service.BaseRepository;
import com.example.petstore.domain.model.Vaccination;
import java.util.UUID;

/**
 * Generated base repository interface for {@link Vaccination}.
 *
 * <p>The service layer depends only on this contract. The persistence implementation is in {@code VaccinationPersistenceAdapter}.
 *
 * <p>This file is always regenerated — add custom query methods to {@code VaccinationRepository} instead.
 */
public interface VaccinationRepositoryBase extends BaseRepository<Vaccination, UUID> {
}
