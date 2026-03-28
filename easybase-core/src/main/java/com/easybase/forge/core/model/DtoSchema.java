package com.easybase.forge.core.model;

import java.util.List;

public record DtoSchema(
		String className, String packageName, List<DtoField> fields, String parentClass, UnionDiscriminator union) {
	public static DtoSchema of(String className, String packageName, List<DtoField> fields) {
		return new DtoSchema(className, packageName, fields, null, null);
	}
}
