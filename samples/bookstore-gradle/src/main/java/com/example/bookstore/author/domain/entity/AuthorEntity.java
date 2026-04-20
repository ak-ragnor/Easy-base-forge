package com.example.bookstore.author.domain.entity;

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
 * JPA entity for the {@code authors} table.
 *
 * <p>This class is always regenerated — do not edit it directly.
 * Custom queries belong in {@code AuthorJpaRepository}.
 */
@Entity
@Table(
    name = "authors"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AuthorEntity {
  @Id
  private UUID id = UUID.randomUUID();

  @Column(
      name = "name",
      nullable = false,
      length = 100
  )
  private String name;

  @Column(
      name = "bio",
      length = 1000
  )
  private String bio;

  @Column(
      name = "email",
      nullable = false,
      length = 255,
      unique = true
  )
  private String email;

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

  @Column(
      name = "tenant_id"
  )
  private UUID tenantId;
}
