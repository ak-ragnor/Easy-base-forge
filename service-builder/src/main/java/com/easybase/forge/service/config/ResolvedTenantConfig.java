package com.easybase.forge.service.config;

/**
 * Fully-resolved tenant configuration. All fields are non-null — generators work
 * exclusively with this view.
 *
 * @param enabled      whether multi-tenancy is active for this entity
 * @param tenantIdType Java type name for the tenant identifier (e.g. {@code UUID}, {@code Long})
 */
public record ResolvedTenantConfig(boolean enabled, String tenantIdType) {}
