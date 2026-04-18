package com.easybase.forge.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServicePackageConfig {

	@JsonProperty("package")
	private String pkg;

	@JsonProperty("basePackage")
	private String basePkg;

	public ServicePackageConfig() {}

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
