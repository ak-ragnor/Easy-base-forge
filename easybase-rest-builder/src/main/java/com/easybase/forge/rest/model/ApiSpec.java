package com.easybase.forge.rest.model;

import java.util.List;

public record ApiSpec(String title, String version, List<ApiResource> resources) {}
