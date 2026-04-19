package com.example.bookstore.books.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

/**
 * @generated
 */
@Data
public class UpdateBookRequest {
    @Size(
            min = 1,
            max = 200
    )
    @JsonProperty("title")
    private String title;

    @Size(
            max = 50
    )
    @JsonProperty("genre")
    private String genre;

    @Min(0L)
    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("coverImageId")
    private UUID coverImageId;
}
