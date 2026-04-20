package com.example.bookstore.book.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generated base domain model for {@code Book}.
 *
 * <p>Contains all generated fields. Do not edit — this file is always regenerated.
 * Add custom fields in the subclass {@code Book}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookBase {
  private UUID id;

  private String title;

  private String isbn;

  private String genre;

  private BigDecimal price;

  private LocalDate publishedDate;

  private UUID authorId;

  private UUID coverImageId;

  private Set<Long> reviewIds = new HashSet<>();

  private Instant createdAt;

  private UUID createdBy;

  private Instant updatedAt;

  private UUID updatedBy;

  private Boolean deleted;

  private UUID tenantId;
}
