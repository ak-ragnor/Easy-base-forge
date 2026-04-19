package com.example.petstore.infrastructure.persistence;

import com.example.petstore.domain.entity.PetEntity;
import com.example.petstore.domain.model.Pet;
import com.example.petstore.infrastructure.hook.base.PetHookBase;
import com.example.petstore.infrastructure.persistence.base.PetPersistenceAdapterBase;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Developer-owned persistence adapter for {@code Pet}.
 *
 * <p>Extend or override methods from {@code PetPersistenceAdapterBase} here.
 * Add custom persistence logic as needed.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class PetPersistenceAdapter extends PetPersistenceAdapterBase {
  public PetPersistenceAdapter(PetJpaRepository petJpaRepository, List<PetHookBase> petHooks) {
    super(petJpaRepository, petHooks);
  }

  @Override
  protected Pet toDomain(PetEntity entity) {
    Pet domain = new Pet();

    domain.setId(entity.getId());
    domain.setName(entity.getName());
    domain.setSpecies(entity.getSpecies());
    domain.setBreed(entity.getBreed());
    domain.setStatus(entity.getStatus());
    domain.setOwnerId(entity.getOwnerId());
    domain.setMedicalRecordId(entity.getMedicalRecordId());

    domain.setCreatedAt(entity.getCreatedAt());
    domain.setCreatedBy(entity.getCreatedBy());
    domain.setUpdatedAt(entity.getUpdatedAt());
    domain.setUpdatedBy(entity.getUpdatedBy());
    domain.setDeleted(entity.getDeleted());

    return domain;
  }

  /**
   * Maps the domain model to a JPA entity.
   * Note: {@code id} is not set — UUID entities self-generate their id.
   * Audit timestamps are managed by Hibernate {@code @CreationTimestamp}/{@code @UpdateTimestamp}.
   */
  @Override
  protected PetEntity toEntity(Pet domain) {
    PetEntity entity = new PetEntity();

    entity.setName(domain.getName());
    entity.setSpecies(domain.getSpecies());
    entity.setBreed(domain.getBreed());
    entity.setStatus(domain.getStatus());
    entity.setOwnerId(domain.getOwnerId());
    entity.setMedicalRecordId(domain.getMedicalRecordId());

    return entity;
  }
}
