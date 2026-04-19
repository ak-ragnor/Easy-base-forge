package com.example.petstore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @generated
 */
@Data
public class UpdateOwnerRequest {
    @Size(
            min = 1,
            max = 100
    )
    @JsonProperty("name")
    private String name;

    @Size(
            max = 20
    )
    @JsonProperty("phone")
    private String phone;
}
