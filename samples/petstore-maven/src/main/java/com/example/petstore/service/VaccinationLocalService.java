package com.example.petstore.service;

import com.example.petstore.infrastructure.repository.VaccinationRepository;
import com.example.petstore.service.base.VaccinationLocalServiceBaseImpl;
import org.springframework.stereotype.Service;

/**
 * Developer-owned local service for {@code Vaccination}.
 *
 * <p>Add business logic and domain-specific operations here.
 * CRUD operations are inherited from {@code VaccinationLocalServiceBaseImpl}.
 *
 * <p>This file is generated once and never overwritten.
 */
@Service
public class VaccinationLocalService extends VaccinationLocalServiceBaseImpl {
  public VaccinationLocalService(VaccinationRepository vaccinationRepository) {
    super(vaccinationRepository);
  }
}
