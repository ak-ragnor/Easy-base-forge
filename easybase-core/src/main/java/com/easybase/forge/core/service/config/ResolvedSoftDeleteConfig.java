package com.easybase.forge.core.service.config;

/**
 * Fully-resolved soft-delete configuration. All fields are non-null — generators work
 * exclusively with this view.
 *
 * @param enabled whether soft-delete is active for this entity
 */
public record ResolvedSoftDeleteConfig(boolean enabled) {}
