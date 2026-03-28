package com.easybase.forge.core.model;

/**
 * A fully-resolved Java type derived from an OpenAPI schema.
 *
 * <p>By the time this record exists, all {@code $ref} chains have been followed,
 * primitives have been mapped to Java types, and collection wrappers applied.
 * Generators must never access the raw OpenAPI model again once they hold an ApiSchema.
 */
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
