package com.example.petstore.service;

import com.example.petstore.service.base.OwnerServiceBaseImpl;
import org.springframework.stereotype.Component;

/**
 * Developer-owned service façade for {@code Owner}.
 *
 * <p>Add cross-cutting concerns (security checks, caching, event publishing) here.
 * All CRUD operations are delegated to {@code OwnerLocalServiceBase} via the base class.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class OwnerService extends OwnerServiceBaseImpl {
  public OwnerService(OwnerLocalService ownerLocalService) {
    super(ownerLocalService);
  }
}
