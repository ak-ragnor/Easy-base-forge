package com.easybase.forge.core.model;

import java.util.List;

public record DtoField(
		String name,
		String jsonName,
		String javaType,
		boolean required,
		List<ValidationConstraint> validations,
		boolean nullable) {
	public static DtoField of(
			String name, String jsonName, String javaType, boolean required, List<ValidationConstraint> validations) {
		return new DtoField(name, jsonName, javaType, required, validations, false);
	}
}
