package com.example.petstore.service;

import com.example.petstore.infrastructure.repository.OwnerRepository;
import com.example.petstore.service.base.OwnerLocalServiceBaseImpl;
import org.springframework.stereotype.Service;

/**
 * Developer-owned local service for {@code Owner}.
 *
 * <p>Add business logic and domain-specific operations here.
 * CRUD operations are inherited from {@code OwnerLocalServiceBaseImpl}.
 *
 * <p>This file is generated once and never overwritten.
 */
@Service
public class OwnerLocalService extends OwnerLocalServiceBaseImpl {
  public OwnerLocalService(OwnerRepository ownerRepository) {
    super(ownerRepository);
  }
}
