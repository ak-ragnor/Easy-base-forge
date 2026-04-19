package com.example.petstore.infrastructure.persistence;

import com.example.petstore.domain.entity.VaccinationEntity;
import com.example.petstore.domain.model.Vaccination;
import com.example.petstore.infrastructure.hook.base.VaccinationHookBase;
import com.example.petstore.infrastructure.persistence.base.VaccinationPersistenceAdapterBase;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Developer-owned persistence adapter for {@code Vaccination}.
 *
 * <p>Extend or override methods from {@code VaccinationPersistenceAdapterBase} here.
 * Add custom persistence logic as needed.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class VaccinationPersistenceAdapter extends VaccinationPersistenceAdapterBase {
  public VaccinationPersistenceAdapter(VaccinationJpaRepository vaccinationJpaRepository,
      List<VaccinationHookBase> vaccinationHooks) {
    super(vaccinationJpaRepository, vaccinationHooks);
  }

  @Override
  protected Vaccination toDomain(VaccinationEntity entity) {
    Vaccination domain = new Vaccination();

    domain.setId(entity.getId());
    domain.setVaccineName(entity.getVaccineName());
    domain.setAdministeredDate(entity.getAdministeredDate());
    domain.setNextDueDate(entity.getNextDueDate());
    domain.setBatchNumber(entity.getBatchNumber());
    domain.setNotes(entity.getNotes());
    domain.setPetId(entity.getPetId());

    domain.setCreatedAt(entity.getCreatedAt());
    domain.setCreatedBy(entity.getCreatedBy());
    domain.setUpdatedAt(entity.getUpdatedAt());
    domain.setUpdatedBy(entity.getUpdatedBy());

    return domain;
  }

  /**
   * Maps the domain model to a JPA entity.
   * Note: {@code id} is not set — UUID entities self-generate their id.
   * Audit timestamps are managed by Hibernate {@code @CreationTimestamp}/{@code @UpdateTimestamp}.
   */
  @Override
  protected VaccinationEntity toEntity(Vaccination domain) {
    VaccinationEntity entity = new VaccinationEntity();

    entity.setVaccineName(domain.getVaccineName());
    entity.setAdministeredDate(domain.getAdministeredDate());
    entity.setNextDueDate(domain.getNextDueDate());
    entity.setBatchNumber(domain.getBatchNumber());
    entity.setNotes(domain.getNotes());
    entity.setPetId(domain.getPetId());

    return entity;
  }
}
