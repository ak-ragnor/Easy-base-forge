package com.example.petstore.service;

import com.example.petstore.service.base.VaccinationServiceBaseImpl;
import org.springframework.stereotype.Component;

/**
 * Developer-owned service façade for {@code Vaccination}.
 *
 * <p>Add cross-cutting concerns (security checks, caching, event publishing) here.
 * All CRUD operations are delegated to {@code VaccinationLocalServiceBase} via the base class.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class VaccinationService extends VaccinationServiceBaseImpl {
  public VaccinationService(VaccinationLocalService vaccinationLocalService) {
    super(vaccinationLocalService);
  }
}
