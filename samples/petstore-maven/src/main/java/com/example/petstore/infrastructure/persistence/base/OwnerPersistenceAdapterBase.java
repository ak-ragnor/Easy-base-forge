package com.example.petstore.infrastructure.persistence.base;

import com.example.petstore.domain.entity.OwnerEntity;
import com.example.petstore.domain.model.Owner;
import com.example.petstore.infrastructure.hook.base.OwnerHookBase;
import com.example.petstore.infrastructure.persistence.OwnerJpaRepository;
import com.example.petstore.infrastructure.repository.OwnerRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Abstract persistence adapter base for {@code Owner}.
 *
 * <p>Bridges {@link OwnerRepository} and Spring Data JPA. Implement {@code toDomain} and {@code toEntity} in {@code OwnerPersistenceAdapter}.
 *
 * <p>This file is always regenerated — do not edit it.
 */
public abstract class OwnerPersistenceAdapterBase implements OwnerRepository {
  protected final OwnerJpaRepository ownerJpaRepository;

  protected final List<OwnerHookBase> ownerHooks;

  protected OwnerPersistenceAdapterBase(OwnerJpaRepository jpaRepo, List<OwnerHookBase> hooks) {
    this.ownerJpaRepository = jpaRepo;
    this.ownerHooks = hooks != null ? hooks : Collections.emptyList();
  }

  @Override
  public Owner create(Owner domain) {
    for (OwnerHookBase h : ownerHooks) {
      h.beforeSave(domain);
    }

    OwnerEntity entity = toEntity(domain);

    Owner saved = toDomain(ownerJpaRepository.save(entity));

    for (OwnerHookBase h : ownerHooks) {
      h.afterSave(saved);
    }

    return saved;
  }

  @Override
  public Owner update(UUID id, Owner domain) {
    for (OwnerHookBase h : ownerHooks) {
      h.beforeUpdate(domain);
    }

    OwnerEntity entity = toEntity(domain);

    Owner saved = toDomain(ownerJpaRepository.save(entity));

    for (OwnerHookBase h : ownerHooks) {
      h.afterUpdate(saved);
    }

    return saved;
  }

  @Override
  public Optional<Owner> findById(UUID id) {
    return ownerJpaRepository.findActiveById(id).map(this::toDomain);
  }

  @Override
  public List<Owner> findAll() {
    List<OwnerEntity> entities = ownerJpaRepository.findAllActive();

    List<Owner> result = new ArrayList<>();

    for (OwnerEntity e : entities) {
      result.add(toDomain(e));
    }

    return result;
  }

  @Override
  public void deleteById(UUID id) {
    for (OwnerHookBase h : ownerHooks) {
      h.beforeDelete(id);
    }

    OwnerEntity entity = ownerJpaRepository.findById(id).orElse(null);

    if (entity != null) {
      entity.setDeleted(true);

      ownerJpaRepository.save(entity);
    }

    for (OwnerHookBase h : ownerHooks) {
      h.afterDelete(id);
    }
  }

  @Override
  public boolean existsById(UUID id) {
    return ownerJpaRepository.existsById(id);
  }

  /**
   * Maps a JPA {@code OwnerEntity} to the domain model {@link Owner}.
   */
  protected abstract Owner toDomain(OwnerEntity entity);

  /**
   * Maps the domain model {@link Owner} to a JPA {@code OwnerEntity}.
   */
  protected abstract OwnerEntity toEntity(Owner domain);
}
