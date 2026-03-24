package com.easybase.forge.core.config;

/**
 * Layout strategy that places all resources in shared (flat) packages.
 *
 * <p>Given pattern {@code {basePackage}.{resource}.controller} and any resource,
 * this produces {@code com.example.controller} — the {@code {resource}} segment is removed.
 *
 * <p><strong>Conflict detection:</strong> when multiple resources generate a DTO with the same
 * class name in the same flat package, the {@link com.easybase.forge.core.writer.GenerationPlan}
 * will throw a {@link com.easybase.forge.core.config.ConfigException} before any files are written.
 */
public class FlatLayoutStrategy implements LayoutStrategy {

	private final String basePackage;

	public FlatLayoutStrategy(String basePackage) {
		this.basePackage = basePackage;
	}

	@Override
	public String resolvePackage(String pattern, String resourceName) {
		return pattern.replace("{basePackage}", basePackage)
				.replace("{Resource}", "")
				.replace("{resource}", "")
				.replaceAll("\\.{2,}", ".")
				.replaceAll("\\.$", "");
	}

	@Override
	public LayoutMode mode() {
		return LayoutMode.FLAT;
	}
}
