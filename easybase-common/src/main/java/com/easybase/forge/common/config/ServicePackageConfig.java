package com.easybase.forge.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Package configuration entry for a service-layer artifact (supports both pkg and basePkg). */
public class ServicePackageConfig {

	@JsonProperty("package")
	private String pkg;

	@JsonProperty("basePackage")
	private String basePkg;

	public ServicePackageConfig() {}

	public ServicePackageConfig(String pkg) {
		this.pkg = pkg;
	}

	public String getPkg() {
		return pkg;
	}

	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

	public String getBasePkg() {
		return basePkg;
	}

	public void setBasePkg(String basePkg) {
		this.basePkg = basePkg;
	}
}
