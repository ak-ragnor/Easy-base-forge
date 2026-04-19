package com.example.petstore.service;

import com.example.petstore.service.base.PetServiceBaseImpl;
import org.springframework.stereotype.Component;

/**
 * Developer-owned service façade for {@code Pet}.
 *
 * <p>Add cross-cutting concerns (security checks, caching, event publishing) here.
 * All CRUD operations are delegated to {@code PetLocalServiceBase} via the base class.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class PetService extends PetServiceBaseImpl {
  public PetService(PetLocalService petLocalService) {
    super(petLocalService);
  }
}
