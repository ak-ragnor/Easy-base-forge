package com.easybase.forge.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Package patterns for the controller layer. */
public class ControllerStructureConfig {

	@JsonProperty("package")
	private String pkg = "{basePackage}.{module}.controller";

	@JsonProperty("basePackage")
	private String basePkg = "{basePackage}.{module}.controller.base";

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
