package com.example.petstore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Data;

/**
 * @generated
 */
@Data
public class UpdatePetRequest {
    @Size(
            min = 1,
            max = 100
    )
    @JsonProperty("name")
    private String name;

    @Size(
            max = 100
    )
    @JsonProperty("breed")
    private String breed;

    @Size(
            max = 20
    )
    @JsonProperty("status")
    private String status;

    @JsonProperty("medicalRecordId")
    private UUID medicalRecordId;
}
