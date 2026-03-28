package com.easybase.forge.core.model;

import java.util.List;

public record ApiParameter(
		String name,
		ParameterLocation in,
		boolean required,
		ApiSchema schema,
		List<ValidationConstraint> validations) {}
