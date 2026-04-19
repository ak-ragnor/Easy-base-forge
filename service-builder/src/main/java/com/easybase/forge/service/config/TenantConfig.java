package com.easybase.forge.service.config;

public class TenantConfig {

	private Boolean enabled;

	private String tenantIdType = "UUID";

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getTenantIdType() {
		return tenantIdType;
	}

	public void setTenantIdType(String tenantIdType) {
		this.tenantIdType = tenantIdType;
	}
}
