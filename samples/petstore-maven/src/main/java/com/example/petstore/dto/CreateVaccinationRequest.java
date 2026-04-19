package com.example.petstore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

/**
 * @generated
 */
@Data
public class CreateVaccinationRequest {
    @NotBlank
    @Size(
            min = 1,
            max = 100
    )
    @JsonProperty("vaccineName")
    private String vaccineName;

    @NotBlank
    @JsonProperty("petId")
    private UUID petId;

    @JsonProperty("administeredDate")
    private LocalDate administeredDate;

    @JsonProperty("nextDueDate")
    private LocalDate nextDueDate;

    @Size(
            max = 50
    )
    @JsonProperty("batchNumber")
    private String batchNumber;

    @Size(
            max = 500
    )
    @JsonProperty("notes")
    private String notes;
}
