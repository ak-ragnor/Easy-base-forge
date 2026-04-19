package com.example.bookstore.books.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * @generated
 */
@Data
public class BookDTO {
    @JsonProperty(
            value = "id",
            access = JsonProperty.Access.READ_ONLY
    )
    private UUID id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("isbn")
    private String isbn;

    @Nullable
    @JsonProperty("genre")
    private String genre;

    @Nullable
    @JsonProperty("price")
    private BigDecimal price;

    @Nullable
    @JsonProperty("publishedDate")
    private LocalDate publishedDate;

    @JsonProperty("authorId")
    private UUID authorId;

    @Nullable
    @JsonProperty("coverImageId")
    private UUID coverImageId;

    @JsonProperty(
            value = "createdAt",
            access = JsonProperty.Access.READ_ONLY
    )
    private OffsetDateTime createdAt;
}
