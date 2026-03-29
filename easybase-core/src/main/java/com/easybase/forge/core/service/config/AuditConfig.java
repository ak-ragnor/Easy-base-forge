package com.easybase.forge.core.service.config;

/**
 * Per-entity audit overrides in {@code easybase.yml}.
 *
 * <p>All fields are nullable — a {@code null} value means "inherit from the project-level
 * defaults in {@code easybase-config.yaml}". Only fields explicitly set in the per-entity
 * YAML override the project defaults.
 *
 * <p>Example YAML (override soft-delete only):
 * <pre>
 * audit:
 *   softDelete: false
 * </pre>
 */
public class AuditConfig {

	/** Overrides {@code service.audit.enabled}. {@code null} means inherit. */
	private Boolean enabled;

	/** Overrides {@code service.audit.auditorType}. {@code null} means inherit. */
	private String auditorType;

	/** Overrides {@code service.audit.softDelete}. {@code null} means inherit. */
	private Boolean softDelete;

	/** Overrides {@code service.audit.softDeleteColumn}. {@code null} means inherit. */
	private String softDeleteColumn;

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getAuditorType() {
		return auditorType;
	}

	public void setAuditorType(String auditorType) {
		this.auditorType = auditorType;
	}

	public Boolean getSoftDelete() {
		return softDelete;
	}

	public void setSoftDelete(Boolean softDelete) {
		this.softDelete = softDelete;
	}

	public String getSoftDeleteColumn() {
		return softDeleteColumn;
	}

	public void setSoftDeleteColumn(String softDeleteColumn) {
		this.softDeleteColumn = softDeleteColumn;
	}
}
