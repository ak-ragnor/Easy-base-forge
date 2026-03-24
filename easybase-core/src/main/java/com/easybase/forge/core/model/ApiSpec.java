package com.easybase.forge.core.model;

import java.util.List;

/** The fully-parsed representation of one OpenAPI document. */
public record ApiSpec(String title, String version, List<ApiResource> resources) {}
