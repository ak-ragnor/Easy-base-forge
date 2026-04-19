package com.easybase.forge.rest.model;

public record ApiRequestBody(boolean required, String contentType, ApiSchema schema) {}
