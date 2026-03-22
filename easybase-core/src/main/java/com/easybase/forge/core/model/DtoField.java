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
        List<ValidationConstraint> validations
) {}
