package com.example.bookstore.review.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generated base domain model for {@code Review}.
 *
 * <p>Contains all generated fields. Do not edit — this file is always regenerated.
 * Add custom fields in the subclass {@code Review}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewBase {
  private Long id;

  private Integer rating;

  private String comment;

  private String reviewerName;

  private UUID bookId;

  private Instant createdAt;

  private UUID createdBy;

  private Instant updatedAt;

  private UUID updatedBy;

  private UUID tenantId;
}
