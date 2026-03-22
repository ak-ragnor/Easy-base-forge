package com.easybase.forge.core.model;

import java.util.List;

public record DtoField(
        /** Java field name (lowerCamelCase). */
        String name,
        /** Original property name from the OpenAPI spec — used for {@code @JsonProperty}. */
        String jsonName,
        /** Resolved Java type (e.g. String, Long, List&lt;PetDTO&gt;). */
        String javaType,
        boolean required,
        List<ValidationConstraint> validations,
        /** True when the OpenAPI schema has {@code nullable: true}. */
        boolean nullable
) {
    /** Convenience factory for non-nullable fields (most common case). */
    public static DtoField of(String name, String jsonName, String javaType,
                               boolean required, List<ValidationConstraint> validations) {
        return new DtoField(name, jsonName, javaType, required, validations, false);
    }
}
