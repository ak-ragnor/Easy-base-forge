package com.example.petstore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * @generated
 */
@Data
public class OwnerDTO {
    @JsonProperty(
            value = "id",
            access = JsonProperty.Access.READ_ONLY
    )
    private UUID id;

    @JsonProperty("name")
    private String name;

    @Email
    @JsonProperty("email")
    private String email;

    @Nullable
    @JsonProperty("phone")
    private String phone;

    @JsonProperty(
            value = "createdAt",
            access = JsonProperty.Access.READ_ONLY
    )
    private OffsetDateTime createdAt;
}
