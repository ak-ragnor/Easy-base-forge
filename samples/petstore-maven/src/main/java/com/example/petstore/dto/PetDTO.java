package com.example.petstore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * @generated
 */
@Data
public class PetDTO {
    @JsonProperty(
            value = "id",
            access = JsonProperty.Access.READ_ONLY
    )
    private UUID id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("species")
    private String species;

    @Nullable
    @JsonProperty("breed")
    private String breed;

    @JsonProperty("status")
    private String status;

    @JsonProperty("ownerId")
    private UUID ownerId;

    @Nullable
    @JsonProperty("medicalRecordId")
    private UUID medicalRecordId;

    @JsonProperty(
            value = "createdAt",
            access = JsonProperty.Access.READ_ONLY
    )
    private OffsetDateTime createdAt;
}
