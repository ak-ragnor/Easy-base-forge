package com.easybase.forge.core.model;

import java.util.List;

public record DtoSchema(
		/** Simple class name, e.g. {@code PetDTO}, {@code CreatePetRequest}. */
		String className,
		/** Fully-qualified package name for this DTO. */
		String packageName,
		List<DtoField> fields,
		/**
		 * Non-null when this DTO is a concrete variant in a discriminated oneOf union.
		 * Holds the simple class name of the generated abstract base class.
		 */
		String parentClass,
		/**
		 * Non-null when this DTO represents the abstract base of a discriminated oneOf union.
		 * Contains the discriminator property name and all variant mappings.
		 */
		UnionDiscriminator union) {
	/** Convenience factory for plain DTOs (no union). */
	public static DtoSchema of(String className, String packageName, List<DtoField> fields) {
		return new DtoSchema(className, packageName, fields, null, null);
	}

	public String fullyQualifiedName() {
		return packageName + "." + className;
	}
}
