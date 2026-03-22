package com.easybase.forge.core.parser;

import com.easybase.forge.core.model.ValidationConstraint;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps OpenAPI schema constraints to {@link ValidationConstraint} instances.
 */
public class ValidationMapper {

    /**
     * Derives validation constraints from a schema for a field that is required in its parent object.
     */
    @SuppressWarnings("rawtypes")
    public List<ValidationConstraint> map(Schema schema, boolean required) {
        List<ValidationConstraint> constraints = new ArrayList<>();
        if (schema == null) return constraints;

        String type = schema.getType();
        String format = schema.getFormat();

        if (required) {
            if ("string".equals(type)) {
                constraints.add(new ValidationConstraint.NotBlank());
            } else {
                constraints.add(new ValidationConstraint.NotNull());
            }
        }

        if ("email".equals(format)) {
            constraints.add(new ValidationConstraint.Email());
        }

        if (schema.getMinLength() != null || schema.getMaxLength() != null) {
            constraints.add(new ValidationConstraint.Size(schema.getMinLength(), schema.getMaxLength()));
        }

        if (schema.getMinimum() != null) {
            constraints.add(new ValidationConstraint.Min(schema.getMinimum().longValue()));
        }

        if (schema.getMaximum() != null) {
            constraints.add(new ValidationConstraint.Max(schema.getMaximum().longValue()));
        }

        if (schema.getPattern() != null) {
            constraints.add(new ValidationConstraint.Pattern(schema.getPattern()));
        }

        return constraints;
    }
}
