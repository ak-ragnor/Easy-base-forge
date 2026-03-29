package com.easybase.forge.core.service.config;

/**
 * The fully-resolved audit configuration after merging project-level defaults with
 * per-entity overrides. All fields are non-null — generators work exclusively with
 * this view.
 *
 * @param enabled          whether audit fields are generated
 * @param auditorType      Java type name for createdBy/updatedBy ({@code UUID}, {@code String}, {@code Long})
 * @param softDelete       whether soft-delete replaces hard deletes
 * @param softDeleteColumn database column name for the soft-delete flag
 */
public record ResolvedAuditConfig(boolean enabled, String auditorType, boolean softDelete, String softDeleteColumn) {}
