package com.easybase.forge.core.parser;

import com.easybase.forge.core.model.ApiSchema;
import com.easybase.forge.core.model.DtoField;
import com.easybase.forge.core.model.DtoSchema;
import com.easybase.forge.core.model.ValidationConstraint;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.*;
import java.util.Objects;

/**
 * Resolves OpenAPI {@link Schema} objects into Java type strings and {@link DtoSchema} definitions.
 *
 * <p>Handles:
 * <ul>
 *   <li>Primitive types (string, integer, number, boolean)</li>
 *   <li>String formats (date, date-time, uuid, email, binary)</li>
 *   <li>Arrays → {@code List<T>}</li>
 *   <li>{@code $ref} to named component schemas</li>
 *   <li>Inline object schemas (generates a DTO with a derived name)</li>
 *   <li>{@code allOf} with two refs — field merging (inheritance)</li>
 *   <li>{@code oneOf} / {@code anyOf} — falls back to {@code Object} with a warning</li>
 * </ul>
 *
 * <p>All {@code $ref} chains are pre-resolved by the Swagger Parser before this class
 * runs, so direct ref strings are only encountered in edge cases.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SchemaResolver {

    private final OpenAPI openApi;
    private final ValidationMapper validationMapper;
    /** Global DTO registry — all named schemas from components and any inline objects. */
    private final Map<String, DtoSchema> dtoRegistry = new LinkedHashMap<>();
    /** Tracks which schemas were referenced during the current resource's endpoint processing. */
    private final Set<String> sessionReferenced = new LinkedHashSet<>();

    public SchemaResolver(OpenAPI openApi, ValidationMapper validationMapper) {
        this.openApi = openApi;
        this.validationMapper = validationMapper;
    }

    /**
     * Resolves the given schema to an {@link ApiSchema} containing the Java type string.
     *
     * @param schema    the OpenAPI schema (may be null)
     * @param hintName  a name hint used when generating a class name for inline objects
     * @return resolved ApiSchema; never null
     */
    public ApiSchema resolve(Schema schema, String hintName) {
        if (schema == null) {
            return ApiSchema.voidSchema();
        }

        // $ref — extract name and mark as referenced in this session
        if (schema.get$ref() != null) {
            String refName = extractRefName(schema.get$ref());
            Schema resolved = resolveRef(refName);
            if (resolved != null) {
                ensureDtoRegistered(refName, resolved);
            }
            sessionReferenced.add(refName);
            return ApiSchema.of(refName);
        }

        // allOf — merge fields (inheritance / composition)
        if (schema instanceof ComposedSchema composed) {
            return resolveComposed(composed, hintName);
        }

        // Array
        if (schema instanceof ArraySchema || "array".equals(schema.getType())) {
            Schema items = schema.getItems();
            ApiSchema itemSchema = resolve(items, hintName + "Item");
            return ApiSchema.ofArray(itemSchema.javaType());
        }

        // Object (inline) — generate a class name from the hint and track it
        if ("object".equals(schema.getType()) || hasProperties(schema)) {
            String className = toPascalCase(hintName);
            ensureDtoRegistered(className, schema);
            sessionReferenced.add(className);
            return ApiSchema.of(className);
        }

        // Primitives
        String javaType = resolvePrimitive(schema);
        boolean isPrimitive = !javaType.startsWith("List") && !javaType.equals("Object");
        return new ApiSchema(javaType, false, isPrimitive, Boolean.TRUE.equals(schema.getNullable()));
    }

    private ApiSchema resolveComposed(ComposedSchema composed, String hintName) {
        List<Schema> allOf = composed.getAllOf();
        if (allOf != null && !allOf.isEmpty()) {
            // Collect all fields from all schemas in allOf
            String className = toPascalCase(hintName);
            List<DtoField> mergedFields = new ArrayList<>();
            for (Schema part : allOf) {
                Schema actual = part.get$ref() != null ? resolveRef(extractRefName(part.get$ref())) : part;
                if (actual != null && actual.getProperties() != null) {
                    Set<String> required = actual.getRequired() != null
                            ? new HashSet<>(actual.getRequired()) : Set.of();
                    mergedFields.addAll(buildFields(actual, required));
                }
            }
            // Use the last $ref name as the class name if available
            String resolvedName = allOf.stream()
                    .filter(s -> s.get$ref() != null)
                    .map(s -> extractRefName(s.get$ref()))
                    .reduce((first, second) -> second)
                    .orElse(className);
            dtoRegistry.put(resolvedName, new DtoSchema(resolvedName, "", mergedFields));
            return ApiSchema.of(resolvedName);
        }

        List<Schema> oneOf = composed.getOneOf();
        List<Schema> anyOf = composed.getAnyOf();
        if ((oneOf != null && !oneOf.isEmpty()) || (anyOf != null && !anyOf.isEmpty())) {
            System.err.println("[WARN] oneOf/anyOf is not fully supported; generating Object for: " + hintName);
            return ApiSchema.of("Object");
        }

        return ApiSchema.of("Object");
    }

    private String resolvePrimitive(Schema schema) {
        String type = schema.getType();
        String format = schema.getFormat();

        if (type == null) return "Object";

        return switch (type) {
            case "string" -> switch (format != null ? format : "") {
                case "date"      -> "LocalDate";
                case "date-time" -> "OffsetDateTime";
                case "uuid"      -> "UUID";
                case "binary"    -> "byte[]";
                case "byte"      -> "byte[]";
                default          -> "String";
            };
            case "integer" -> "int64".equals(format) ? "Long" : "Integer";
            case "number"  -> "float".equals(format) ? "Float" : "BigDecimal";
            case "boolean" -> "Boolean";
            default        -> "Object";
        };
    }

    /** Ensures a named schema from the components section is registered as a DtoSchema. */
    public void ensureDtoRegistered(String name, Schema schema) {
        if (dtoRegistry.containsKey(name)) return;
        if (schema == null) return;

        // Placeholder to prevent infinite recursion on self-referential schemas
        dtoRegistry.put(name, new DtoSchema(name, "", List.of()));

        Set<String> required = schema.getRequired() != null
                ? new HashSet<>(schema.getRequired()) : Set.of();
        List<DtoField> fields = buildFields(schema, required);
        dtoRegistry.put(name, new DtoSchema(name, "", fields));
    }

    private List<DtoField> buildFields(Schema schema, Set<String> required) {
        List<DtoField> fields = new ArrayList<>();
        if (schema.getProperties() == null) return fields;

        schema.getProperties().forEach((propName, propSchema) -> {
            String fieldName = toLowerCamelCase((String) propName);
            boolean isRequired = required.contains(propName);
            ApiSchema fieldType = resolve((Schema) propSchema, toPascalCase((String) propName));
            List<ValidationConstraint> constraints = validationMapper.map((Schema) propSchema, isRequired);
            fields.add(new DtoField(fieldName, (String) propName, fieldType.javaType(), isRequired, constraints));
        });

        return fields;
    }

    /** Returns all DTOs in the global registry, keyed by class name. */
    public Map<String, DtoSchema> getDtoRegistry() {
        return Collections.unmodifiableMap(dtoRegistry);
    }

    /**
     * Returns only the DTOs referenced during the current session (since last {@link #resetSession()}).
     * Used to determine which DTOs belong to the resource currently being processed.
     */
    public List<DtoSchema> getSessionDtos() {
        return sessionReferenced.stream()
                .map(dtoRegistry::get)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Marks a schema name as referenced in the current session without registering a new schema.
     * Used when walking already-resolved type names from built endpoints.
     */
    public void ensureSessionTracked(String className) {
        if (className != null && dtoRegistry.containsKey(className)) {
            sessionReferenced.add(className);
        }
    }

    /** Clears the session reference tracker. Call before processing each new resource. */
    public void resetSession() {
        sessionReferenced.clear();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Schema resolveRef(String name) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) return null;
        return openApi.getComponents().getSchemas().get(name);
    }

    private static String extractRefName(String ref) {
        if (ref == null) return null;
        int idx = ref.lastIndexOf('/');
        return idx >= 0 ? ref.substring(idx + 1) : ref;
    }

    private static boolean hasProperties(Schema schema) {
        return schema.getProperties() != null && !schema.getProperties().isEmpty();
    }

    static String toPascalCase(String name) {
        if (name == null || name.isEmpty()) return name;
        // Handle snake_case and kebab-case by splitting on _ and -
        String[] parts = name.split("[_\\-]");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.length() > 1 ? part.substring(1) : "");
            }
        }
        return sb.toString();
    }

    static String toLowerCamelCase(String name) {
        String pascal = toPascalCase(name);
        if (pascal.isEmpty()) return pascal;
        return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
    }
}
