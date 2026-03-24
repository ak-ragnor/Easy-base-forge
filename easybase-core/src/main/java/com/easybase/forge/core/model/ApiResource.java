package com.easybase.forge.core.model;

import java.util.List;

/**
 * Represents one logical REST resource (e.g. "Pet", "Order") derived from
 * an OpenAPI tag or path prefix.
 */
public record ApiResource(String name, String packageSuffix, List<ApiEndpoint> endpoints, List<DtoSchema> dtoSchemas) {}
