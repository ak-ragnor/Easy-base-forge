package com.example.petstore.infrastructure.persistence.base;

import com.example.petstore.domain.entity.PetEntity;
import com.example.petstore.domain.model.Pet;
import com.example.petstore.infrastructure.hook.base.PetHookBase;
import com.example.petstore.infrastructure.persistence.PetJpaRepository;
import com.example.petstore.infrastructure.repository.PetRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Abstract persistence adapter base for {@code Pet}.
 *
 * <p>Bridges {@link PetRepository} and Spring Data JPA. Implement {@code toDomain} and {@code toEntity} in {@code PetPersistenceAdapter}.
 *
 * <p>This file is always regenerated — do not edit it.
 */
public abstract class PetPersistenceAdapterBase implements PetRepository {
  protected final PetJpaRepository petJpaRepository;

  protected final List<PetHookBase> petHooks;

  protected PetPersistenceAdapterBase(PetJpaRepository jpaRepo, List<PetHookBase> hooks) {
    this.petJpaRepository = jpaRepo;
    this.petHooks = Collections.emptyList();

    if (hooks != null) {
      this.petHooks = hooks;
    }
  }

  @Override
  public Pet create(Pet domain) {
    for (PetHookBase h : petHooks) {
      h.beforeSave(domain);
    }

    PetEntity entity = toEntity(domain);

    Pet saved = toDomain(petJpaRepository.save(entity));

    for (PetHookBase h : petHooks) {
      h.afterSave(saved);
    }

    return saved;
  }

  @Override
  public Pet update(UUID id, Pet domain) {
    for (PetHookBase h : petHooks) {
      h.beforeUpdate(domain);
    }

    PetEntity entity = toEntity(domain);

    Pet saved = toDomain(petJpaRepository.save(entity));

    for (PetHookBase h : petHooks) {
      h.afterUpdate(saved);
    }

    return saved;
  }

  @Override
  public Optional<Pet> findById(UUID id) {
    return petJpaRepository.findActiveById(id).map(this::toDomain);
  }

  @Override
  public List<Pet> findAll() {
    List<PetEntity> entities = petJpaRepository.findAllActive();

    List<Pet> result = new ArrayList<>();

    for (PetEntity e : entities) {
      result.add(toDomain(e));
    }

    return result;
  }

  @Override
  public void deleteById(UUID id) {
    for (PetHookBase h : petHooks) {
      h.beforeDelete(id);
    }

    PetEntity entity = petJpaRepository.findById(id).orElse(null);

    if (entity != null) {
      entity.setDeleted(true);

      petJpaRepository.save(entity);
    }

    for (PetHookBase h : petHooks) {
      h.afterDelete(id);
    }
  }

  @Override
  public boolean existsById(UUID id) {
    return petJpaRepository.existsById(id);
  }

  /**
   * Maps a JPA {@code PetEntity} to the domain model {@link Pet}.
   */
  protected abstract Pet toDomain(PetEntity entity);

  /**
   * Maps the domain model {@link Pet} to a JPA {@code PetEntity}.
   */
  protected abstract PetEntity toEntity(Pet domain);
}
