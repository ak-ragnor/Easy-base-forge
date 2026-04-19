package com.easybase.forge.rest.model;

import java.util.List;

public record ApiResource(String name, String packageSuffix, List<ApiEndpoint> endpoints, List<DtoSchema> dtoSchemas) {}
