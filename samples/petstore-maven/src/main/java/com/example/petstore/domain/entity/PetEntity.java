package com.example.petstore.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA entity for the {@code eb_pets} table.
 *
 * <p>This class is always regenerated — do not edit it directly.
 * Custom queries belong in {@code PetJpaRepository}.
 */
@Entity
@Table(
    name = "eb_pets"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PetEntity {
  @Id
  private UUID id = UUID.randomUUID();

  @Column(
      name = "name",
      nullable = false,
      length = 100
  )
  private String name;

  @Column(
      name = "species",
      nullable = false,
      length = 50
  )
  private String species;

  @Column(
      name = "breed",
      length = 100
  )
  private String breed;

  @Column(
      name = "status",
      nullable = false,
      length = 20
  )
  private String status;

  @Column(
      name = "owner_id",
      nullable = false
  )
  private UUID ownerId;

  @Column(
      name = "medical_record_id",
      nullable = true
  )
  private UUID medicalRecordId;

  @CreationTimestamp
  @Column(
      name = "created_at",
      updatable = false
  )
  private Instant createdAt;

  @Column(
      name = "created_by",
      updatable = false
  )
  private UUID createdBy;

  @UpdateTimestamp
  @Column(
      name = "updated_at"
  )
  private Instant updatedAt;

  @Column(
      name = "updated_by"
  )
  private UUID updatedBy;

  @Column(
      name = "deleted"
  )
  private Boolean deleted = false;
}
