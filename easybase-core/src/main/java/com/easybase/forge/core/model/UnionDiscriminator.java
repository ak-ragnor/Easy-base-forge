package com.easybase.forge.core.model;

import java.util.List;

/**
 * Describes a discriminated union (OpenAPI {@code oneOf} with a {@code discriminator} block).
 *
 * <p>Stored on the abstract-base {@link DtoSchema} produced for a discriminated oneOf.
 * The concrete variant schemas have their {@link DtoSchema#parentClass()} set to this
 * base class name.
 */
public record UnionDiscriminator(
        /** The JSON property name that carries the type discriminator (e.g. {@code "type"}). */
        String propertyName,
        /** Ordered list of discriminator-value → concrete class name mappings. */
        List<SubtypeMapping> subtypes
) {
    /**
     * Maps one discriminator value (the string that appears in JSON) to a concrete DTO class name.
     */
    public record SubtypeMapping(String discriminatorValue, String className) {}
}
