package com.example.petstore.service;

import com.example.petstore.infrastructure.repository.PetRepository;
import com.example.petstore.service.base.PetLocalServiceBaseImpl;
import org.springframework.stereotype.Service;

/**
 * Developer-owned local service for {@code Pet}.
 *
 * <p>Add business logic and domain-specific operations here.
 * CRUD operations are inherited from {@code PetLocalServiceBaseImpl}.
 *
 * <p>This file is generated once and never overwritten.
 */
@Service
public class PetLocalService extends PetLocalServiceBaseImpl {
  public PetLocalService(PetRepository petRepository) {
    super(petRepository);
  }
}
