package com.example.bookstore.books.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

/**
 * @generated
 */
@Data
public class CreateBookRequest {
    @NotBlank
    @Size(
            min = 1,
            max = 200
    )
    @JsonProperty("title")
    private String title;

    @NotBlank
    @Size(
            min = 10,
            max = 20
    )
    @JsonProperty("isbn")
    private String isbn;

    @Size(
            max = 50
    )
    @JsonProperty("genre")
    private String genre;

    @Min(0L)
    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("publishedDate")
    private LocalDate publishedDate;

    @NotBlank
    @JsonProperty("authorId")
    private UUID authorId;

    @JsonProperty("coverImageId")
    private UUID coverImageId;
}
