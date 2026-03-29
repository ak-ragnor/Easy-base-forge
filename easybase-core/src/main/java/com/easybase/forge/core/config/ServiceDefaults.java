package com.easybase.forge.core.config;

/**
 * Project-level service builder defaults read from the {@code service} block in
 * {@code easybase-config.yaml}.
 *
 * <p>Example YAML:
 * <pre>
 * service:
 *   audit:
 *     enabled: true
 *     auditorType: UUID
 *     softDelete: true
 *     softDeleteColumn: deleted
 * </pre>
 */
public class ServiceDefaults {

	private AuditDefaults audit = new AuditDefaults();

	public AuditDefaults getAudit() {
		return audit;
	}

	public void setAudit(AuditDefaults audit) {
		this.audit = audit;
	}
}
