package com.example.petstore.infrastructure.persistence.base;

import com.example.petstore.domain.entity.VaccinationEntity;
import com.example.petstore.domain.model.Vaccination;
import com.example.petstore.infrastructure.hook.base.VaccinationHookBase;
import com.example.petstore.infrastructure.persistence.VaccinationJpaRepository;
import com.example.petstore.infrastructure.repository.VaccinationRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Abstract persistence adapter base for {@code Vaccination}.
 *
 * <p>Bridges {@link VaccinationRepository} and Spring Data JPA. Implement {@code toDomain} and {@code toEntity} in {@code VaccinationPersistenceAdapter}.
 *
 * <p>This file is always regenerated — do not edit it.
 */
public abstract class VaccinationPersistenceAdapterBase implements VaccinationRepository {
  protected final VaccinationJpaRepository vaccinationJpaRepository;

  protected final List<VaccinationHookBase> vaccinationHooks;

  protected VaccinationPersistenceAdapterBase(VaccinationJpaRepository jpaRepo,
      List<VaccinationHookBase> hooks) {
    this.vaccinationJpaRepository = jpaRepo;
    this.vaccinationHooks = Collections.emptyList();

    if (hooks != null) {
      this.vaccinationHooks = hooks;
    }
  }

  @Override
  public Vaccination create(Vaccination domain) {
    for (VaccinationHookBase h : vaccinationHooks) {
      h.beforeSave(domain);
    }

    VaccinationEntity entity = toEntity(domain);

    Vaccination saved = toDomain(vaccinationJpaRepository.save(entity));

    for (VaccinationHookBase h : vaccinationHooks) {
      h.afterSave(saved);
    }

    return saved;
  }

  @Override
  public Vaccination update(UUID id, Vaccination domain) {
    for (VaccinationHookBase h : vaccinationHooks) {
      h.beforeUpdate(domain);
    }

    VaccinationEntity entity = toEntity(domain);

    Vaccination saved = toDomain(vaccinationJpaRepository.save(entity));

    for (VaccinationHookBase h : vaccinationHooks) {
      h.afterUpdate(saved);
    }

    return saved;
  }

  @Override
  public Optional<Vaccination> findById(UUID id) {
    return vaccinationJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public List<Vaccination> findAll() {
    List<VaccinationEntity> entities = vaccinationJpaRepository.findAll();

    List<Vaccination> result = new ArrayList<>();

    for (VaccinationEntity e : entities) {
      result.add(toDomain(e));
    }

    return result;
  }

  @Override
  public void deleteById(UUID id) {
    for (VaccinationHookBase h : vaccinationHooks) {
      h.beforeDelete(id);
    }

    vaccinationJpaRepository.deleteById(id);

    for (VaccinationHookBase h : vaccinationHooks) {
      h.afterDelete(id);
    }
  }

  @Override
  public boolean existsById(UUID id) {
    return vaccinationJpaRepository.existsById(id);
  }

  /**
   * Maps a JPA {@code VaccinationEntity} to the domain model {@link Vaccination}.
   */
  protected abstract Vaccination toDomain(VaccinationEntity entity);

  /**
   * Maps the domain model {@link Vaccination} to a JPA {@code VaccinationEntity}.
   */
  protected abstract VaccinationEntity toEntity(Vaccination domain);
}
