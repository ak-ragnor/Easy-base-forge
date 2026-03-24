package com.easybase.forge.core.config;

/**
 * Layout strategy that produces per-resource sub-packages.
 *
 * <p>Given pattern {@code {basePackage}.{resource}.controller} and resource {@code pets},
 * this produces {@code com.example.pets.controller}.
 */
public class MultiModuleLayoutStrategy implements LayoutStrategy {

	private final String basePackage;

	public MultiModuleLayoutStrategy(String basePackage) {
		this.basePackage = basePackage;
	}

	@Override
	public String resolvePackage(String pattern, String resourceName) {
		final String pascalCase = toPascalCase(resourceName);

		return pattern.replace("{basePackage}", basePackage)
				.replace("{Resource}", pascalCase)
				.replace("{resource}", resourceName.toLowerCase());
	}

	@Override
	public LayoutMode mode() {
		return LayoutMode.MULTI_MODULE;
	}

	private static String toPascalCase(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}
		return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
	}
}
