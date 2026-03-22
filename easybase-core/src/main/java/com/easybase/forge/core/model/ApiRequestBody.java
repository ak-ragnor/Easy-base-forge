package com.easybase.forge.core.model;

public record ApiRequestBody(
        boolean required,
        String contentType,
        ApiSchema schema
) {}
