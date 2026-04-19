package com.easybase.forge.rest.model;

public sealed interface ValidationConstraint
		permits ValidationConstraint.NotNull,
				ValidationConstraint.NotBlank,
				ValidationConstraint.Email,
				ValidationConstraint.Size,
				ValidationConstraint.Min,
				ValidationConstraint.Max,
				ValidationConstraint.Pattern {

	record NotNull() implements ValidationConstraint {}

	record NotBlank() implements ValidationConstraint {}

	record Email() implements ValidationConstraint {}

	record Size(Integer min, Integer max) implements ValidationConstraint {}

	record Min(long value) implements ValidationConstraint {}

	record Max(long value) implements ValidationConstraint {}

	record Pattern(String regexp) implements ValidationConstraint {}
}
