package com.easybase.forge.core.config.layout;

import com.easybase.forge.core.config.LayoutMode;

public interface LayoutStrategy {

	String resolvePackage(String pattern, String moduleName);

	/** The {@link LayoutMode} this strategy implements. */
	LayoutMode mode();
}
