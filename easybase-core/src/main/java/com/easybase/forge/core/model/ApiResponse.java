package com.easybase.forge.core.model;

public record ApiResponse(
        int statusCode,
        String description,
        /** Null for 204 No Content or operations with no response body. */
        ApiSchema schema
) {}
