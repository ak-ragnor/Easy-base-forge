package com.easybase.forge.rest.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.easybase.forge.rest.model.ApiSchema;
import com.easybase.forge.rest.model.DtoField;
import com.easybase.forge.rest.model.DtoSchema;
import com.easybase.forge.rest.model.UnionDiscriminator;
import com.easybase.forge.rest.model.ValidationConstraint;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Schema;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SchemaResolver {

	private final OpenAPI openApi;
	private final ValidationMapper validationMapper;
	private final Map<String, DtoSchema> dtoRegistry = new LinkedHashMap<>();
	private final Set<String> sessionReferenced = new LinkedHashSet<>();

	public SchemaResolver(OpenAPI openApi, ValidationMapper validationMapper) {
		this.openApi = openApi;
		this.validationMapper = validationMapper;
	}

	public ApiSchema resolve(Schema schema, String hintName) {
		if (schema == null) {
			return ApiSchema.voidSchema();
		}

		if (schema.get$ref() != null) {
			String refName = extractRefName(schema.get$ref());
			Schema resolved = resolveRef(refName);

			if (resolved instanceof ComposedSchema composedResolved) {
				return resolveComposed(composedResolved, refName);
			}

			if (resolved != null) {
				ensureDtoRegistered(refName, resolved);
			}
			ensureTransitivesTracked(refName);
			return ApiSchema.of(refName);
		}

		if (schema instanceof ComposedSchema composed) {
			return resolveComposed(composed, hintName);
		}

		if (schema instanceof ArraySchema || "array".equals(schema.getType())) {
			Schema items = schema.getItems();
			ApiSchema itemSchema = resolve(items, hintName + "Item");
			return ApiSchema.ofArray(itemSchema.javaType());
		}

		if ("object".equals(schema.getType()) || hasProperties(schema)) {
			String className = toPascalCase(hintName);
			ensureDtoRegistered(className, schema);
			ensureTransitivesTracked(className);
			return ApiSchema.of(className);
		}

		String javaType = resolvePrimitive(schema);
		boolean isPrimitive = !javaType.startsWith("List") && !javaType.equals("Object");
		return new ApiSchema(javaType, false, isPrimitive, Boolean.TRUE.equals(schema.getNullable()));
	}

	private ApiSchema resolveComposed(ComposedSchema composed, String hintName) {
		List<Schema> allOf = composed.getAllOf();

		if (allOf != null && !allOf.isEmpty()) {
			return resolveAllOf(allOf, hintName);
		}

		List<Schema> oneOf = composed.getOneOf();
		List<Schema> anyOf = composed.getAnyOf();
		List<Schema> variants = null;

		if (oneOf != null && !oneOf.isEmpty()) {
			variants = oneOf;
		} else if (anyOf != null && !anyOf.isEmpty()) {
			variants = anyOf;
		}

		if (variants != null) {
			return resolveOneOfAnyOf(variants, composed.getDiscriminator(), hintName);
		}

		return ApiSchema.of("Object");
	}

	private ApiSchema resolveAllOf(List<Schema> allOf, String hintName) {
		String className = toPascalCase(hintName);
		List<DtoField> mergedFields = new ArrayList<>();

		for (Schema part : allOf) {
			Schema actual = part;

			if (part.get$ref() != null) {
				actual = resolveRef(extractRefName(part.get$ref()));
			}

			if (actual != null && actual.getProperties() != null) {
				Set<String> required = Set.of();

				if (actual.getRequired() != null) {
					required = new HashSet<>(actual.getRequired());
				}

				mergedFields.addAll(buildFields(actual, required));
			}
		}

		String resolvedName = allOf.stream()
				.filter(s -> s.get$ref() != null)
				.map(s -> extractRefName(s.get$ref()))
				.reduce((first, second) -> second)
				.orElse(className);

		dtoRegistry.put(resolvedName, DtoSchema.of(resolvedName, "", mergedFields));
		sessionReferenced.add(resolvedName);
		return ApiSchema.of(resolvedName);
	}

	private ApiSchema resolveOneOfAnyOf(List<Schema> variants, Discriminator discriminator, String hintName) {
		List<String> variantNames = new ArrayList<>();

		for (Schema variant : variants) {
			if (variant.get$ref() != null) {
				String refName = extractRefName(variant.get$ref());
				Schema resolved = resolveRef(refName);
				if (resolved != null) {
					ensureDtoRegistered(refName, resolved);
				}
				sessionReferenced.add(refName);
				variantNames.add(refName);
			}
		}

		if (discriminator == null || discriminator.getPropertyName() == null || variantNames.isEmpty()) {
			return ApiSchema.of("Object");
		}

		String baseName = toPascalCase(hintName);
		Map<String, String> explicitMapping = Map.of();

		if (discriminator.getMapping() != null) {
			explicitMapping = discriminator.getMapping();
		}

		List<UnionDiscriminator.SubtypeMapping> subtypes = new ArrayList<>();
		for (String variantName : variantNames) {
			String discValue = explicitMapping.entrySet().stream()
					.filter(e -> extractRefName(e.getValue()).equals(variantName))
					.map(Map.Entry::getKey)
					.findFirst()
					.orElse(toLowerCamelCase(variantName));
			subtypes.add(new UnionDiscriminator.SubtypeMapping(discValue, variantName));
		}

		UnionDiscriminator union = new UnionDiscriminator(discriminator.getPropertyName(), subtypes);
		dtoRegistry.put(baseName, new DtoSchema(baseName, "", List.of(), null, union));
		sessionReferenced.add(baseName);

		for (String variantName : variantNames) {
			DtoSchema existing = dtoRegistry.get(variantName);
			if (existing != null) {
				dtoRegistry.put(
						variantName,
						new DtoSchema(
								existing.className(),
								existing.packageName(),
								existing.fields(),
								baseName,
								existing.union()));
			}
		}

		return ApiSchema.of(baseName);
	}

	private String resolvePrimitive(Schema schema) {
		String type = schema.getType();
		String format = schema.getFormat();

		if (type == null) {
			return "Object";
		}

		return switch (type) {
			case "string" -> {
				String fmt = format != null ? format : "";
				yield switch (fmt) {
					case "date" -> "LocalDate";
					case "date-time" -> "OffsetDateTime";
					case "uuid" -> "UUID";
					case "binary", "byte" -> "byte[]";
					default -> "String";
				};
			}
			case "integer" -> "int64".equals(format) ? "Long" : "Integer";
			case "number" -> "float".equals(format) ? "Float" : "BigDecimal";
			case "boolean" -> "Boolean";
			default -> "Object";
		};
	}

	public void ensureDtoRegistered(String name, Schema schema) {
		if (dtoRegistry.containsKey(name) || schema == null) {
			return;
		}

		if (schema instanceof ComposedSchema composed) {
			resolveComposed(composed, name);
			return;
		}

		dtoRegistry.put(name, DtoSchema.of(name, "", List.of()));
		Set<String> required = Set.of();

		if (schema.getRequired() != null) {
			required = new HashSet<>(schema.getRequired());
		}

		List<DtoField> fields = buildFields(schema, required);
		dtoRegistry.put(name, DtoSchema.of(name, "", fields));
	}

	@SuppressWarnings("unchecked")
	private List<DtoField> buildFields(Schema schema, Set<String> required) {
		if (schema.getProperties() == null) {
			return List.of();
		}

		Map<String, Schema> properties = (Map<String, Schema>) schema.getProperties();

		return properties.entrySet().stream()
				.map(entry -> buildField(entry.getKey(), entry.getValue(), required))
				.collect(Collectors.toList());
	}

	private DtoField buildField(String propName, Schema propSchema, Set<String> required) {
		String fieldName = toLowerCamelCase(propName);
		boolean isRequired = required.contains(propName);
		ApiSchema fieldType = resolve(propSchema, toPascalCase(propName));
		List<ValidationConstraint> constraints = validationMapper.map(propSchema, isRequired);
		boolean isNullable = fieldType.nullable();
		boolean isReadOnly = Boolean.TRUE.equals(propSchema.getReadOnly());

		return new DtoField(fieldName, propName, fieldType.javaType(), isRequired, constraints, isNullable, isReadOnly);
	}

	public List<DtoSchema> getSessionDtos() {
		return sessionReferenced.stream()
				.map(dtoRegistry::get)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private void ensureTransitivesTracked(String className) {
		if (className == null || !dtoRegistry.containsKey(className) || !sessionReferenced.add(className)) {
			return;
		}

		DtoSchema dto = dtoRegistry.get(className);

		if (dto == null) {
			return;
		}

		for (DtoField field : dto.fields()) {
			String fieldType = field.javaType();

			if (fieldType.startsWith("List<") && fieldType.endsWith(">")) {
				fieldType = fieldType.substring(5, fieldType.length() - 1);
			}

			if (dtoRegistry.containsKey(fieldType)) {
				ensureTransitivesTracked(fieldType);
			}
		}
	}

	public void resetSession() {
		sessionReferenced.clear();
	}

	private Schema resolveRef(String name) {
		if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
			return null;
		}

		return openApi.getComponents().getSchemas().get(name);
	}

	private static String extractRefName(String ref) {
		if (ref == null) {
			return null;
		}

		int idx = ref.lastIndexOf('/');

		if (idx >= 0) {
			return ref.substring(idx + 1);
		}

		return ref;
	}

	private static boolean hasProperties(Schema schema) {
		return schema.getProperties() != null && !schema.getProperties().isEmpty();
	}

	static String toPascalCase(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}

		String[] parts = name.split("[_\\-]");
		StringBuilder sb = new StringBuilder();

		for (String part : parts) {
			if (!part.isEmpty()) {
				sb.append(Character.toUpperCase(part.charAt(0)));

				if (part.length() > 1) {
					sb.append(part.substring(1));
				}
			}
		}

		return sb.toString();
	}

	static String toLowerCamelCase(String name) {
		String pascal = toPascalCase(name);

		if (pascal.isEmpty()) {
			return pascal;
		}

		return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
	}
}
