package com.example.petstore.infrastructure.persistence;

import com.example.petstore.domain.entity.OwnerEntity;
import com.example.petstore.domain.model.Owner;
import com.example.petstore.infrastructure.hook.base.OwnerHookBase;
import com.example.petstore.infrastructure.persistence.base.OwnerPersistenceAdapterBase;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Developer-owned persistence adapter for {@code Owner}.
 *
 * <p>Extend or override methods from {@code OwnerPersistenceAdapterBase} here.
 * Add custom persistence logic as needed.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class OwnerPersistenceAdapter extends OwnerPersistenceAdapterBase {
  public OwnerPersistenceAdapter(OwnerJpaRepository ownerJpaRepository,
      List<OwnerHookBase> ownerHooks) {
    super(ownerJpaRepository, ownerHooks);
  }

  @Override
  protected Owner toDomain(OwnerEntity entity) {
    Owner domain = new Owner();

    domain.setId(entity.getId());
    domain.setName(entity.getName());
    domain.setEmail(entity.getEmail());
    domain.setPhone(entity.getPhone());

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
  protected OwnerEntity toEntity(Owner domain) {
    OwnerEntity entity = new OwnerEntity();

    entity.setName(domain.getName());
    entity.setEmail(domain.getEmail());
    entity.setPhone(domain.getPhone());

    return entity;
  }
}
