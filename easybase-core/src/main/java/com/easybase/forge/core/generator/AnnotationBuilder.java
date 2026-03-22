package com.easybase.forge.core.generator;

import com.easybase.forge.core.model.ValidationConstraint;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

/**
 * Builds JavaPoet {@link AnnotationSpec} instances from {@link ValidationConstraint} sealed types.
 */
public class AnnotationBuilder {

    private static final String JAKARTA_CONSTRAINTS = "jakarta.validation.constraints";

    public static AnnotationSpec build(ValidationConstraint constraint) {
        // instanceof pattern matching (Java 16+) — switch patterns on sealed types need Java 21
        if (constraint instanceof ValidationConstraint.NotNull) {
            return AnnotationSpec.builder(ClassName.get(JAKARTA_CONSTRAINTS, "NotNull")).build();
        }
        if (constraint instanceof ValidationConstraint.NotBlank) {
            return AnnotationSpec.builder(ClassName.get(JAKARTA_CONSTRAINTS, "NotBlank")).build();
        }
        if (constraint instanceof ValidationConstraint.Email) {
            return AnnotationSpec.builder(ClassName.get(JAKARTA_CONSTRAINTS, "Email")).build();
        }
        if (constraint instanceof ValidationConstraint.Size s) {
            AnnotationSpec.Builder b = AnnotationSpec.builder(ClassName.get(JAKARTA_CONSTRAINTS, "Size"));
            if (s.min() != null) b.addMember("min", "$L", s.min());
            if (s.max() != null) b.addMember("max", "$L", s.max());
            return b.build();
        }
        if (constraint instanceof ValidationConstraint.Min m) {
            return AnnotationSpec.builder(ClassName.get(JAKARTA_CONSTRAINTS, "Min"))
                    .addMember("value", "$LL", m.value())
                    .build();
        }
        if (constraint instanceof ValidationConstraint.Max m) {
            return AnnotationSpec.builder(ClassName.get(JAKARTA_CONSTRAINTS, "Max"))
                    .addMember("value", "$LL", m.value())
                    .build();
        }
        if (constraint instanceof ValidationConstraint.Pattern p) {
            return AnnotationSpec.builder(ClassName.get(JAKARTA_CONSTRAINTS, "Pattern"))
                    .addMember("regexp", "$S", p.regexp())
                    .build();
        }
        throw new IllegalArgumentException("Unknown ValidationConstraint type: " + constraint.getClass());
    }
}
