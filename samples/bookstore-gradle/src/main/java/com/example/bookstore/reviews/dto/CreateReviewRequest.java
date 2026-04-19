package com.example.bookstore.reviews.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Data;

/**
 * @generated
 */
@Data
public class CreateReviewRequest {
    @NotNull
    @Min(1L)
    @Max(5L)
    @JsonProperty("rating")
    private Integer rating;

    @Size(
            max = 2000
    )
    @JsonProperty("comment")
    private String comment;

    @NotBlank
    @Size(
            min = 1,
            max = 100
    )
    @JsonProperty("reviewerName")
    private String reviewerName;

    @NotBlank
    @JsonProperty("bookId")
    private UUID bookId;
}
