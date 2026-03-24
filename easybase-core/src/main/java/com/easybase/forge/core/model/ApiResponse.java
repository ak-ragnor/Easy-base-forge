package com.easybase.forge.core.model;

public record ApiResponse(int statusCode, String description, ApiSchema schema) {}
