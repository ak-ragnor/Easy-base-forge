package com.easybase.forge.rest.model;

public record ApiSchema(String javaType, boolean isArray, boolean isPrimitive, boolean nullable) {
	public static ApiSchema of(String javaType) {
		return new ApiSchema(javaType, false, false, false);
	}

	public static ApiSchema ofArray(String elementType) {
		return new ApiSchema("List<" + elementType + ">", true, false, false);
	}

	public static ApiSchema voidSchema() {
		return new ApiSchema("Void", false, false, false);
	}
}
