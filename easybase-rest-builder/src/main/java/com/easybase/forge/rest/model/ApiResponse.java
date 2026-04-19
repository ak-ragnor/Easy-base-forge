package com.easybase.forge.rest.model;

public record ApiResponse(int statusCode, String description, ApiSchema schema) {}
