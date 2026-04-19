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
 * Generated base domain model for {@code Owner}.
 *
 * <p>Contains all generated fields. Do not edit — this file is always regenerated.
 * Add custom fields in the subclass {@code Owner}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerBase {
  private UUID id;

  private String name;

  private String email;

  private String phone;

  private Set<UUID> petIds = new HashSet<>();

  private Instant createdAt;

  private UUID createdBy;

  private Instant updatedAt;

  private UUID updatedBy;

  private Boolean deleted;
}
