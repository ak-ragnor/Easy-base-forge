package com.easybase.forge.core.model;

/**
 * Represents a bean validation constraint derived from OpenAPI schema properties.
 * Maps to Jakarta Validation annotations on generated DTOs.
 */
public sealed interface ValidationConstraint
		permits ValidationConstraint.NotNull,
				ValidationConstraint.NotBlank,
				ValidationConstraint.Email,
				ValidationConstraint.Size,
				ValidationConstraint.Min,
				ValidationConstraint.Max,
				ValidationConstraint.Pattern {

	/** Maps to {@code @NotNull}. Applied to required fields of non-string type. */
	record NotNull() implements ValidationConstraint {}

	/** Maps to {@code @NotBlank}. Applied to required string fields. */
	record NotBlank() implements ValidationConstraint {}

	/** Maps to {@code @Email}. Applied to string fields with {@code format: email}. */
	record Email() implements ValidationConstraint {}

	/** Maps to {@code @Size(min=..., max=...)}. */
	record Size(Integer min, Integer max) implements ValidationConstraint {}

	/** Maps to {@code @Min(value)}. */
	record Min(long value) implements ValidationConstraint {}

	/** Maps to {@code @Max(value)}. */
	record Max(long value) implements ValidationConstraint {}

	/** Maps to {@code @Pattern(regexp=...)}. */
	record Pattern(String regexp) implements ValidationConstraint {}
}
