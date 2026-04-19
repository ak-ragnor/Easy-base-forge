package com.example.petstore.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA entity for the {@code eb_vaccinations} table.
 *
 * <p>This class is always regenerated — do not edit it directly.
 * Custom queries belong in {@code VaccinationJpaRepository}.
 */
@Entity
@Table(
    name = "eb_vaccinations"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VaccinationEntity {
  @Id
  private UUID id = UUID.randomUUID();

  @Column(
      name = "vaccine_name",
      nullable = false,
      length = 100
  )
  private String vaccineName;

  @Column(
      name = "administered_date"
  )
  private LocalDate administeredDate;

  @Column(
      name = "next_due_date"
  )
  private LocalDate nextDueDate;

  @Column(
      name = "batch_number",
      length = 50
  )
  private String batchNumber;

  @Column(
      name = "notes",
      length = 500
  )
  private String notes;

  @Column(
      name = "pet_id",
      nullable = false
  )
  private UUID petId;

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
}
