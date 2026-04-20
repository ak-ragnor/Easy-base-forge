package com.example.bookstore.book.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
 * JPA entity for the {@code books} table.
 *
 * <p>This class is always regenerated — do not edit it directly.
 * Custom queries belong in {@code BookJpaRepository}.
 */
@Entity
@Table(
    name = "books"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookEntity {
  @Id
  private UUID id = UUID.randomUUID();

  @Column(
      name = "title",
      nullable = false,
      length = 200
  )
  private String title;

  @Column(
      name = "isbn",
      nullable = false,
      length = 20,
      unique = true
  )
  private String isbn;

  @Column(
      name = "genre",
      length = 50
  )
  private String genre;

  @Column(
      name = "price"
  )
  private BigDecimal price;

  @Column(
      name = "published_date"
  )
  private LocalDate publishedDate;

  @Column(
      name = "author_id",
      nullable = false
  )
  private UUID authorId;

  @Column(
      name = "cover_image_id",
      nullable = true
  )
  private UUID coverImageId;

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
