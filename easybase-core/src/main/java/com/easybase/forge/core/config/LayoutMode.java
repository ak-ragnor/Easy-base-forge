package com.easybase.forge.core.config;

public enum LayoutMode {
    /** One sub-package per resource (e.g. com.example.api.user.controller). Default. */
    MULTI_MODULE,
    /** All resources share top-level packages (e.g. com.example.api.controller). */
    FLAT
}
