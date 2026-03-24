package com.easybase.forge.core.config;

/**
 * Strategy for resolving Java package names from config patterns.
 *
 * <p>Two implementations exist:
 * <ul>
 *   <li>{@link MultiModuleLayoutStrategy} — substitutes {@code {resource}} with the actual resource
 *       name, producing per-resource sub-packages (e.g. {@code com.example.pets.controller}).</li>
 *   <li>{@link FlatLayoutStrategy} — strips the {@code {resource}} segment, placing all resources
 *       in shared packages (e.g. {@code com.example.controller}).</li>
 * </ul>
 *
 * <p>Obtain an instance via {@link GeneratorConfig#getLayoutStrategy()}.
 */
public interface LayoutStrategy {

	/**
	 * Resolves a package pattern by substituting {@code {basePackage}}, {@code {resource}},
	 * and {@code {Resource}} placeholders.
	 *
	 * @param pattern      package pattern string from {@code easybase-config.yaml}
	 * @param resourceName the resource name (e.g. {@code "pets"})
	 * @return fully-qualified package name
	 */
	String resolvePackage(String pattern, String resourceName);

	/** The {@link LayoutMode} this strategy implements. */
	LayoutMode mode();
}
