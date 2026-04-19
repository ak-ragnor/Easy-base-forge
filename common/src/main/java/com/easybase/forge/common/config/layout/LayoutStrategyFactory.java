package com.easybase.forge.common.config.layout;

import com.easybase.forge.common.config.LayoutMode;

/** Creates the appropriate {@link LayoutStrategy} for a given {@link LayoutMode}. */
public final class LayoutStrategyFactory {

	private LayoutStrategyFactory() {}

	/**
	 * @param mode        the layout mode; if {@code null}, defaults to {@link LayoutMode#FLAT}
	 * @param basePackage the root package for all generated types
	 * @return a new strategy instance; never {@code null}
	 */
	public static LayoutStrategy create(LayoutMode mode, String basePackage) {
		if (mode == LayoutMode.MULTI_MODULE) {
			return new MultiModuleLayoutStrategy(basePackage);
		}

		return new FlatLayoutStrategy(basePackage);
	}
}
