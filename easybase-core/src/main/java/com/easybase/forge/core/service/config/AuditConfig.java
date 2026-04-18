package com.easybase.forge.core.service.config;

public class AuditConfig {

	private Boolean enabled;

	private String auditorType;

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
}
