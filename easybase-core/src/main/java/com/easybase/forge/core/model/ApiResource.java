package com.easybase.forge.core.model;

import java.util.List;

/**
 * Represents one logical REST resource (e.g. "Pet", "Order") derived from
 * an OpenAPI tag or path prefix.
 */
public record ApiResource(
        /** PascalCase resource name, e.g. {@code Pet}, {@code Order}. */
        String name,
        /** Lowercase resource name used for package interpolation, e.g. {@code pet}. */
        String packageSuffix,
        List<ApiEndpoint> endpoints,
        /** All DTO schemas referenced by this resource's operations. */
        List<DtoSchema> dtoSchemas
) {}
