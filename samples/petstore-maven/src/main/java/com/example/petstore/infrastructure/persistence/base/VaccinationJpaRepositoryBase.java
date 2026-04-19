package com.example.petstore.infrastructure.persistence.base;

import com.example.petstore.domain.entity.VaccinationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Generated Spring Data JPA repository base for {@code VaccinationEntity}.
 *
 * <p>Used internally by {@code VaccinationPersistenceAdapterBase}. Do not inject this interface into service-layer beans.
 *
 * <p>This file is always regenerated — add custom JPQL queries to {@code VaccinationJpaRepository} instead.
 */
@NoRepositoryBean
public interface VaccinationJpaRepositoryBase extends JpaRepository<VaccinationEntity, UUID> {
}
