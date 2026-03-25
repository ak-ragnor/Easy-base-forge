package com.easybase.forge.core.generator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.easybase.forge.core.config.PaginationMode;
import com.easybase.forge.core.config.ResponseEntityMode;
import com.easybase.forge.core.config.ResponseWrapperConfig;
import com.easybase.forge.core.model.ApiEndpoint;
import com.easybase.forge.core.model.ApiResponse;
import com.squareup.javapoet.*;

/**
 * Converts a Java type string (as produced by {@link com.easybase.forge.core.parser.SchemaResolver})
 * into a JavaPoet {@link TypeName}.
 *
 * <p>Unknown types (i.e. DTO class names) are resolved against {@code dtoPkg}.
 */
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
		TypeName body = (javaType == null || javaType.equals("Void") || javaType.isBlank())
				? ClassName.get(Void.class)
				: resolve(javaType);

		return ParameterizedTypeName.get(ClassName.get("org.springframework.http", "ResponseEntity"), body);
	}

	/**
	 * Converts a Java type string to {@code Page<T>} for paginated endpoints.
	 *
	 * <p>If the type is already a {@code List<X>}, the element type {@code X} is used directly.
	 * Otherwise the full type is used as-is (e.g. {@code Page<PetDTO>}).
	 */
	public TypeName page(String javaType) {
		String elementType = stripListWrapper(javaType);

		TypeName element = (elementType == null || elementType.isBlank() || elementType.equals("Void"))
				? ClassName.get(Object.class)
				: resolve(elementType);

		return ParameterizedTypeName.get(ClassName.get("org.springframework.data.domain", "Page"), element);
	}

	/**
	 * Wraps a type in {@code ResponseEntity<Page<T>>} for paginated endpoints.
	 */
	public TypeName responseEntityPage(String javaType) {
		return ParameterizedTypeName.get(ClassName.get("org.springframework.http", "ResponseEntity"), page(javaType));
	}

	/** The {@code Pageable} type from Spring Data. */
	public static TypeName pageableType() {
		return ClassName.get("org.springframework.data.domain", "Pageable");
	}

	/**
	 * Returns {@code WrapperClass<T>} where the wrapper FQCN is provided at runtime.
	 * Used for custom single-item response wrappers such as {@code ApiResponse<T>}.
	 */
	public TypeName customWrapper(String fqcn, String javaType) {
		ClassName wrapper = fqcnToClassName(fqcn);
		TypeName body = (javaType == null || javaType.isBlank() || javaType.equals("Void"))
				? ClassName.get(Void.class)
				: resolve(javaType);

		return ParameterizedTypeName.get(wrapper, body);
	}

	/**
	 * Returns {@code WrapperClass<T>} for paginated endpoints.
	 *
	 * <p>{@code ApiPageResponse<T>} takes {@code T} directly (not {@code List<T>}),
	 * so if {@code javaType} is {@code "List<X>"} the element type {@code X} is extracted.
	 */
	public TypeName customWrapperPage(String fqcn, String javaType) {
		String elementType = stripListWrapper(javaType);

		ClassName wrapper = fqcnToClassName(fqcn);
		TypeName element = (elementType == null || elementType.isBlank() || elementType.equals("Void"))
				? ClassName.get(Object.class)
				: resolve(elementType);

		return ParameterizedTypeName.get(wrapper, element);
	}

	/**
	 * Single authoritative return-type resolution used by all three generators.
	 *
	 * <p>When {@link ResponseWrapperConfig#isEnabled()} is true the custom wrapper
	 * classes take precedence over the {@link ResponseEntityMode} switch.
	 */
	public TypeName resolveReturnType(
			ApiEndpoint endpoint,
			ResponseEntityMode mode,
			ResponseWrapperConfig wrapper,
			PaginationMode paginationMode) {

		boolean applyPagination = endpoint.paginated() && paginationMode == PaginationMode.SPRING_DATA;

		ApiResponse primary = endpoint.primaryResponse();
		String bodyType =
				(primary != null && primary.schema() != null) ? primary.schema().javaType() : null;
		boolean isVoid = bodyType == null || bodyType.equals("Void");

		if (wrapper != null && wrapper.isEnabled()) {
			if (applyPagination && !isVoid){
				return customWrapperPage(wrapper.getPagedClass(), bodyType);
			}

			if (!isVoid){
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
			case NEVER -> isVoid ? TypeName.VOID : resolve(bodyType);
			case VOID_ONLY -> isVoid ? responseEntity(null) : resolve(bodyType);
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
