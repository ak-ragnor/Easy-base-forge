package com.example.petstore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * @generated
 */
@Data
public class VaccinationDTO {
    @JsonProperty(
            value = "id",
            access = JsonProperty.Access.READ_ONLY
    )
    private UUID id;

    @JsonProperty("vaccineName")
    private String vaccineName;

    @JsonProperty("petId")
    private UUID petId;

    @Nullable
    @JsonProperty("administeredDate")
    private LocalDate administeredDate;

    @Nullable
    @JsonProperty("nextDueDate")
    private LocalDate nextDueDate;

    @Nullable
    @JsonProperty("batchNumber")
    private String batchNumber;

    @Nullable
    @JsonProperty("notes")
    private String notes;

    @JsonProperty(
            value = "createdAt",
            access = JsonProperty.Access.READ_ONLY
    )
    private OffsetDateTime createdAt;
}
