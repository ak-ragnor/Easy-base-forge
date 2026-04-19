package com.example.bookstore.authors.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @generated
 */
@Data
public class CreateAuthorRequest {
    @NotBlank
    @Size(
            min = 1,
            max = 100
    )
    @JsonProperty("name")
    private String name;

    @Size(
            max = 1000
    )
    @JsonProperty("bio")
    private String bio;

    @NotBlank
    @Email
    @Size(
            max = 255
    )
    @JsonProperty("email")
    private String email;
}
