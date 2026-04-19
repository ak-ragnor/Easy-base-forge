package com.easybase.forge.rest.generator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.easybase.forge.common.config.PaginationMode;
import com.easybase.forge.common.config.ResponseEntityMode;
import com.easybase.forge.common.config.ResponseWrapperConfig;
import com.easybase.forge.rest.model.ApiEndpoint;
import com.easybase.forge.rest.model.ApiResponse;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

public class TypeNameResolver {

	private final String dtoPkg;

	public TypeNameResolver(String dtoPkg) {
		this.dtoPkg = dtoPkg;
	}

	public TypeName resolve(String javaType) {
		if (javaType == null || javaType.isBlank()) {
			return TypeName.VOID;
		}

		if (javaType.startsWith("List<") && javaType.endsWith(">")) {
			String inner = javaType.substring(5, javaType.length() - 1);

			return ParameterizedTypeName.get(ClassName.get("java.util", "List"), resolve(inner));
		}

		return switch (javaType) {
			case "void", "Void" -> TypeName.VOID.box();
			case "String" -> ClassName.get(String.class);
			case "Integer" -> ClassName.get(Integer.class);
			case "Long" -> ClassName.get(Long.class);
			case "Boolean" -> ClassName.get(Boolean.class);
			case "Float" -> ClassName.get(Float.class);
			case "BigDecimal" -> ClassName.get(BigDecimal.class);
			case "LocalDate" -> ClassName.get(LocalDate.class);
			case "OffsetDateTime" -> ClassName.get(OffsetDateTime.class);
			case "UUID" -> ClassName.get(UUID.class);
			case "Object" -> ClassName.get(Object.class);
			case "byte[]" -> ArrayTypeName.of(TypeName.BYTE);
			default -> ClassName.get(dtoPkg, javaType);
		};
	}

	public TypeName responseEntity(String javaType) {
		TypeName body;

		if (javaType == null || javaType.isBlank() || javaType.equals("Void")) {
			body = ClassName.get(Void.class);
		} else {
			body = resolve(javaType);
		}

		return ParameterizedTypeName.get(ClassName.get("org.springframework.http", "ResponseEntity"), body);
	}

	public TypeName page(String javaType) {
		String elementType = stripListWrapper(javaType);
		TypeName element;

		if (elementType == null || elementType.isBlank() || elementType.equals("Void")) {
			element = ClassName.get(Object.class);
		} else {
			element = resolve(elementType);
		}

		return ParameterizedTypeName.get(ClassName.get("org.springframework.data.domain", "Page"), element);
	}

	public TypeName responseEntityPage(String javaType) {
		return ParameterizedTypeName.get(ClassName.get("org.springframework.http", "ResponseEntity"), page(javaType));
	}

	public static TypeName pageableType() {
		return ClassName.get("org.springframework.data.domain", "Pageable");
	}

	public TypeName customWrapper(String fqcn, String javaType) {
		TypeName body;

		if (javaType == null || javaType.isBlank() || javaType.equals("Void")) {
			body = ClassName.get(Void.class);
		} else {
			body = resolve(javaType);
		}

		return ParameterizedTypeName.get(fqcnToClassName(fqcn), body);
	}

	public TypeName customWrapperPage(String fqcn, String javaType) {
		String elementType = stripListWrapper(javaType);
		TypeName element;

		if (elementType == null || elementType.isBlank() || elementType.equals("Void")) {
			element = ClassName.get(Object.class);
		} else {
			element = resolve(elementType);
		}

		return ParameterizedTypeName.get(fqcnToClassName(fqcn), element);
	}

	public TypeName resolveReturnType(
			ApiEndpoint endpoint,
			ResponseEntityMode mode,
			ResponseWrapperConfig wrapper,
			PaginationMode paginationMode) {

		boolean applyPagination = endpoint.paginated() && paginationMode == PaginationMode.SPRING_DATA;
		ApiResponse primary = endpoint.primaryResponse();
		String bodyType = null;

		if (primary != null && primary.schema() != null) {
			bodyType = primary.schema().javaType();
		}

		boolean isVoid = bodyType == null || bodyType.equals("Void");

		if (wrapper != null && wrapper.isEnabled()) {
			if (applyPagination && !isVoid) {
				return customWrapperPage(wrapper.getPagedClass(), bodyType);
			}

			if (!isVoid) {
				return customWrapper(wrapper.getSingleClass(), bodyType);
			}

			return TypeName.VOID;
		}

		if (applyPagination && !isVoid) {
			return switch (mode) {
				case ALWAYS -> responseEntityPage(bodyType);
				case NEVER, VOID_ONLY -> page(bodyType);
			};
		}

		return switch (mode) {
			case ALWAYS -> responseEntity(bodyType);
			case NEVER -> {
				if (isVoid) {
					yield TypeName.VOID;
				}

				yield resolve(bodyType);
			}
			case VOID_ONLY -> {
				if (isVoid) {
					yield responseEntity(null);
				}

				yield resolve(bodyType);
			}
		};
	}

	private static String stripListWrapper(String javaType) {
		if (javaType != null && javaType.startsWith("List<") && javaType.endsWith(">")) {
			return javaType.substring(5, javaType.length() - 1);
		}

		return javaType;
	}

	private static ClassName fqcnToClassName(String fqcn) {
		int dot = fqcn.lastIndexOf('.');

		if (dot < 0) {
			return ClassName.get("", fqcn);
		}

		return ClassName.get(fqcn.substring(0, dot), fqcn.substring(dot + 1));
	}
}
