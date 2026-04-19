package com.example.petstore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @generated
 */
@Data
public class CreateOwnerRequest {
    @NotBlank
    @Size(
            min = 1,
            max = 100
    )
    @JsonProperty("name")
    private String name;

    @NotBlank
    @Email
    @Size(
            max = 255
    )
    @JsonProperty("email")
    private String email;

    @Size(
            max = 20
    )
    @JsonProperty("phone")
    private String phone;
}
