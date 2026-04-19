package com.example.petstore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Data;

/**
 * @generated
 */
@Data
public class CreatePetRequest {
    @NotBlank
    @Size(
            min = 1,
            max = 100
    )
    @JsonProperty("name")
    private String name;

    @NotBlank
    @Size(
            min = 1,
            max = 50
    )
    @JsonProperty("species")
    private String species;

    @Size(
            max = 100
    )
    @JsonProperty("breed")
    private String breed;

    @NotBlank
    @Size(
            max = 20
    )
    @JsonProperty("status")
    private String status;

    @NotBlank
    @JsonProperty("ownerId")
    private UUID ownerId;

    @JsonProperty("medicalRecordId")
    private UUID medicalRecordId;
}
