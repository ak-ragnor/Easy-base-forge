package com.easybase.forge.service.generator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.easybase.forge.service.config.ServiceConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

public final class ServiceTypeResolver {

	private ServiceTypeResolver() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static TypeName resolve(String javaType, String contextPkg) {
		if (javaType == null || javaType.isBlank()) {
			return TypeName.VOID;
		}

		return switch (javaType) {
			case "String" -> ClassName.get(String.class);
			case "Integer" -> ClassName.get(Integer.class);
			case "Long" -> ClassName.get(Long.class);
			case "Boolean" -> ClassName.get(Boolean.class);
			case "BigDecimal" -> ClassName.get(BigDecimal.class);
			case "LocalDate" -> ClassName.get(LocalDate.class);
			case "OffsetDateTime" -> ClassName.get(OffsetDateTime.class);
			case "UUID" -> ClassName.get(UUID.class);
			case "Instant" -> ClassName.get(Instant.class);
			default -> ClassName.get(contextPkg, javaType);
		};
	}

	public static TypeName resolveIdType(ServiceConfig config) {
		return resolve(config.getIdType(), config.getBasePackage());
	}
}
