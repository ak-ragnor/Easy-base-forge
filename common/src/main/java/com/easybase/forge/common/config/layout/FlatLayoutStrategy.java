package com.easybase.forge.common.config.layout;

import com.easybase.forge.common.config.LayoutMode;

/** All resources share top-level packages — {@code {module}} tokens are stripped. */
public class FlatLayoutStrategy implements LayoutStrategy {

	private final String basePackage;

	public FlatLayoutStrategy(String basePackage) {
		this.basePackage = basePackage;
	}

	@Override
	public String resolvePackage(String pattern, String moduleName) {
		return pattern.replace("{basePackage}", basePackage)
				.replace(".{module}", "")
				.replace("{module}.", "")
				.replace("{module}", "")
				.replaceAll("\\.{2,}", ".")
				.replaceAll("\\.$", "");
	}

	@Override
	public LayoutMode mode() {
		return LayoutMode.FLAT;
	}
}
