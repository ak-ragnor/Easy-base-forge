package com.example.petstore.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generated base domain model for {@code Vaccination}.
 *
 * <p>Contains all generated fields. Do not edit — this file is always regenerated.
 * Add custom fields in the subclass {@code Vaccination}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VaccinationBase {
  private UUID id;

  private String vaccineName;

  private LocalDate administeredDate;

  private LocalDate nextDueDate;

  private String batchNumber;

  private String notes;

  private UUID petId;

  private Instant createdAt;

  private UUID createdBy;

  private Instant updatedAt;

  private UUID updatedBy;
}
