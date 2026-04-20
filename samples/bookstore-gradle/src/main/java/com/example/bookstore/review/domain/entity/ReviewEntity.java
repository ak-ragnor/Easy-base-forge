package com.example.bookstore.review.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
 * JPA entity for the {@code reviews} table.
 *
 * <p>This class is always regenerated — do not edit it directly.
 * Custom queries belong in {@code ReviewJpaRepository}.
 */
@Entity
@Table(
    name = "reviews"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReviewEntity {
  @Id
  @GeneratedValue(
      strategy = GenerationType.IDENTITY
  )
  private Long id;

  @Column(
      name = "rating",
      nullable = false
  )
  private Integer rating;

  @Column(
      name = "comment",
      length = 2000
  )
  private String comment;

  @Column(
      name = "reviewer_name",
      nullable = false,
      length = 100
  )
  private String reviewerName;

  @Column(
      name = "book_id",
      nullable = false
  )
  private UUID bookId;

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
      name = "tenant_id"
  )
  private UUID tenantId;
}
