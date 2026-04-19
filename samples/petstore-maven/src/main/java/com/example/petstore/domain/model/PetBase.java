package com.example.petstore.domain.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generated base domain model for {@code Pet}.
 *
 * <p>Contains all generated fields. Do not edit — this file is always regenerated.
 * Add custom fields in the subclass {@code Pet}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetBase {
  private UUID id;

  private String name;

  private String species;

  private String breed;

  private String status;

  private UUID ownerId;

  private UUID medicalRecordId;

  private Set<UUID> vaccinationIds = new HashSet<>();

  private Instant createdAt;

  private UUID createdBy;

  private Instant updatedAt;

  private UUID updatedBy;

  private Boolean deleted;
}
