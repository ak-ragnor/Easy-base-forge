package com.easybase.forge.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Package configuration for controller artifacts.
 *
 * <p>Supports placeholders: {@code {basePackage}}, {@code {resource}}, {@code {Resource}}.
 */
public class ControllerStructureConfig {

	@JsonProperty("package")
	private String pkg = "{basePackage}.{resource}.controller";

	@JsonProperty("basePackage")
	private String basePkg = "{basePackage}.{resource}.controller.base";

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
