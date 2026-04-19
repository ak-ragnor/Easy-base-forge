package com.example.bookstore.reviews.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * @generated
 */
@Data
public class ReviewDTO {
    @JsonProperty(
            value = "id",
            access = JsonProperty.Access.READ_ONLY
    )
    private Long id;

    @Min(1L)
    @Max(5L)
    @JsonProperty("rating")
    private Integer rating;

    @Nullable
    @JsonProperty("comment")
    private String comment;

    @JsonProperty("reviewerName")
    private String reviewerName;

    @JsonProperty("bookId")
    private UUID bookId;

    @JsonProperty(
            value = "createdAt",
            access = JsonProperty.Access.READ_ONLY
    )
    private OffsetDateTime createdAt;
}
