package com.easybase.forge.common.config.layout;

import com.easybase.forge.common.config.LayoutMode;

/** Strategy for resolving package patterns to concrete package names. */
public interface LayoutStrategy {

	/** Resolves a package pattern (e.g. {@code "{basePackage}.{module}.dto"}) to a concrete name. */
	String resolvePackage(String pattern, String moduleName);

	/** The {@link LayoutMode} this strategy implements. */
	LayoutMode mode();
}
