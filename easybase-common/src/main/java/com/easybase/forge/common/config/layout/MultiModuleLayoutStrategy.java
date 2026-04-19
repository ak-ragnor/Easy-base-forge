package com.easybase.forge.common.config.layout;

import com.easybase.forge.common.config.LayoutMode;

/** Each resource gets its own sub-package tree — {@code {module}} is replaced with the resource name. */
public class MultiModuleLayoutStrategy implements LayoutStrategy {

	private final String basePackage;

	public MultiModuleLayoutStrategy(String basePackage) {
		this.basePackage = basePackage;
	}

	@Override
	public String resolvePackage(String pattern, String moduleName) {
		return pattern.replace("{basePackage}", basePackage).replace("{module}", moduleName.toLowerCase());
	}

	@Override
	public LayoutMode mode() {
		return LayoutMode.MULTI_MODULE;
	}
}
