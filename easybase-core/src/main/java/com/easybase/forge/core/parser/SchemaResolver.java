package com.easybase.forge.core.parser;

import com.easybase.forge.core.model.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Schema;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Resolves OpenAPI {@link Schema} objects into Java type strings and {@link DtoSchema} definitions.
 *
 * <p>Handles:
 * <ul>
 *   <li>Primitive types (string, integer, number, boolean)</li>
 *   <li>String formats (date, date-time, uuid, email, binary)</li>
 *   <li>Arrays → {@code List<T>}</li>
 *   <li>{@code $ref} to named component schemas (including composed schemas)</li>
 *   <li>Inline object schemas (generates a DTO with a derived name)</li>
 *   <li>{@code allOf} with two refs — field merging (inheritance)</li>
 *   <li>{@code oneOf} with discriminator → abstract base with {@code @JsonTypeInfo} /
 *       {@code @JsonSubTypes}; each variant extends the base</li>
 *   <li>{@code oneOf} without discriminator / {@code anyOf} → all variant DTOs generated,
 *       return type is {@code Object}</li>
 * </ul>
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

        // $ref — extract component name and resolve
        if (schema.get$ref() != null) {
            String refName = extractRefName(schema.get$ref());
            Schema resolved = resolveRef(refName);

            if (resolved instanceof ComposedSchema composedResolved) {
                // $ref pointing at a oneOf/anyOf/allOf — resolve through composition.
                // The returned type may differ from refName (e.g. "AnimalBase" for discriminated oneOf).
                return resolveComposed(composedResolved, refName);
            }

            if (resolved != null) {
                ensureDtoRegistered(refName, resolved);
            }
            sessionReferenced.add(refName);
            return ApiSchema.of(refName);
        }

        // Inline allOf / oneOf / anyOf
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
            return resolveAllOf(allOf, hintName);
        }

        List<Schema> oneOf = composed.getOneOf();
        List<Schema> anyOf = composed.getAnyOf();
        List<Schema> variants = oneOf != null && !oneOf.isEmpty() ? oneOf
                : anyOf != null && !anyOf.isEmpty() ? anyOf
                : null;

        if (variants != null) {
            return resolveOneOfAnyOf(variants, composed.getDiscriminator(), hintName);
        }

        return ApiSchema.of("Object");
    }

    private ApiSchema resolveAllOf(List<Schema> allOf, String hintName) {
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
        dtoRegistry.put(resolvedName, DtoSchema.of(resolvedName, "", mergedFields));
        sessionReferenced.add(resolvedName);
        return ApiSchema.of(resolvedName);
    }

    /**
     * Handles {@code oneOf} and {@code anyOf}:
     *
     * <ul>
     *   <li>All {@code $ref} variants are resolved and registered as DTOs.</li>
     *   <li>With a discriminator: an abstract base class is generated with Jackson
     *       {@code @JsonTypeInfo} / {@code @JsonSubTypes} metadata. Each variant DTO is
     *       patched with {@link DtoSchema#parentClass()} so the generator emits
     *       {@code extends BaseClass}. The returned type is the base class name.</li>
     *   <li>Without a discriminator: all variants are still registered; returned type is
     *       {@code Object}.</li>
     * </ul>
     */
    private ApiSchema resolveOneOfAnyOf(List<Schema> variants, Discriminator discriminator,
                                        String hintName) {
        // Register all variant schemas as DTOs and track in session
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
            // Inline schemas in oneOf are uncommon; skipped silently
        }

        if (discriminator == null || discriminator.getPropertyName() == null
                || variantNames.isEmpty()) {
            // No discriminator — variants generated, but union type is Object
            return ApiSchema.of("Object");
        }

        // Discriminated union — build the abstract base class
        String baseName = toPascalCase(hintName) + "Base";
        Map<String, String> explicitMapping = discriminator.getMapping() != null
                ? discriminator.getMapping() : Map.of();

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

        // Patch each variant DTO to record its parent (enables `extends BaseClass` in generator)
        for (String variantName : variantNames) {
            DtoSchema existing = dtoRegistry.get(variantName);
            if (existing != null) {
                dtoRegistry.put(variantName, new DtoSchema(
                        existing.className(), existing.packageName(),
                        existing.fields(), baseName, existing.union()));
            }
        }

        return ApiSchema.of(baseName);
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

    /**
     * Ensures a named schema is registered as a {@link DtoSchema}.
     *
     * <p>If the schema is a {@link ComposedSchema} (oneOf/anyOf/allOf), delegates to
     * {@link #resolveComposed(ComposedSchema, String)} so that variants and/or the abstract
     * base are properly registered without creating a spurious plain DTO for the composed name.
     */
    public void ensureDtoRegistered(String name, Schema schema) {
        if (dtoRegistry.containsKey(name)) return;
        if (schema == null) return;

        if (schema instanceof ComposedSchema composed) {
            // Composition schemas are handled via resolveComposed; don't register the
            // composite name itself as a plain DTO.
            resolveComposed(composed, name);
            return;
        }

        // Placeholder to prevent infinite recursion on self-referential schemas
        dtoRegistry.put(name, DtoSchema.of(name, "", List.of()));

        Set<String> required = schema.getRequired() != null
                ? new HashSet<>(schema.getRequired()) : Set.of();
        List<DtoField> fields = buildFields(schema, required);
        dtoRegistry.put(name, DtoSchema.of(name, "", fields));
    }

    private List<DtoField> buildFields(Schema schema, Set<String> required) {
        List<DtoField> fields = new ArrayList<>();
        if (schema.getProperties() == null) return fields;

        schema.getProperties().forEach((propName, propSchema) -> {
            String fieldName = toLowerCamelCase((String) propName);
            boolean isRequired = required.contains(propName);
            ApiSchema fieldType = resolve((Schema) propSchema, toPascalCase((String) propName));
            List<ValidationConstraint> constraints = validationMapper.map((Schema) propSchema, isRequired);
            boolean isNullable = fieldType.nullable();
            fields.add(new DtoField(fieldName, (String) propName, fieldType.javaType(),
                    isRequired, constraints, isNullable));
        });

        return fields;
    }

    /** Returns all DTOs in the global registry, keyed by class name. */
    public Map<String, DtoSchema> getDtoRegistry() {
        return Collections.unmodifiableMap(dtoRegistry);
    }

    /**
     * Returns only the DTOs referenced during the current session (since last {@link #resetSession()}).
     */
    public List<DtoSchema> getSessionDtos() {
        return sessionReferenced.stream()
                .map(dtoRegistry::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Marks a schema name as referenced in the current session.
     * Used by {@link ResourceExtractor} for types already resolved into endpoint records.
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
