package com.easybase.forge.core.config;

import java.nio.file.Path;

/**
 * Root configuration object parsed from {@code easybase-config.yaml}.
 *
 * <p>Example YAML:
 * <pre>
 * basePackage: com.example.api
 * output:
 *   directory: ../api-rest/src/main/java
 *   layout: MULTI_MODULE
 * structure:
 *   controller:
 *     package: "{basePackage}.{resource}.controller"
 *     basePackage: "{basePackage}.{resource}.controller.base"
 *   delegate:
 *     package: "{basePackage}.{resource}.delegate"
 *   dto:
 *     package: "{basePackage}.{resource}.dto"
 * generate:
 *   delegateImpl: false
 *   responseEntityWrapping: ALWAYS
 *   beanValidation: true
 *   pagination: NONE
 * </pre>
 */
public class GeneratorConfig {

	private String basePackage;
	private OutputConfig output = new OutputConfig();
	private StructureConfig structure = new StructureConfig();
	private GenerateOptions generate = new GenerateOptions();

	private Path resolvedOutputDirectory;

	private transient LayoutStrategy layoutStrategy;

	public String getBasePackage() {
		return basePackage;
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
		this.layoutStrategy = null;
	}

	public OutputConfig getOutput() {
		return output;
	}

	public void setOutput(OutputConfig output) {
		this.output = output;
		this.layoutStrategy = null;
	}

	public StructureConfig getStructure() {
		return structure;
	}

	public void setStructure(StructureConfig structure) {
		this.structure = structure;
	}

	public GenerateOptions getGenerate() {
		return generate;
	}

	public void setGenerate(GenerateOptions generate) {
		this.generate = generate;
	}

	public Path getResolvedOutputDirectory() {
		return resolvedOutputDirectory;
	}

	public GeneratorConfig withOutputDirectory(Path outputDirectory) {
		this.resolvedOutputDirectory = outputDirectory;
		return this;
	}

	/**
	 * Returns the {@link LayoutStrategy} for this configuration.
	 *
	 * <p>Defaults to {@link FlatLayoutStrategy} when no {@code output.layout} is set.
	 */
	public LayoutStrategy getLayoutStrategy() {
		if (layoutStrategy == null) {
			LayoutMode mode = (output != null && output.getLayout() != null) ? output.getLayout() : LayoutMode.FLAT;
			layoutStrategy = LayoutStrategyFactory.create(mode, basePackage);
		}

		return layoutStrategy;
	}

	/**
	 * Resolve a package pattern by substituting {@code {basePackage}}, {@code {resource}},
	 * and {@code {Resource}} placeholders.
	 *
	 * <p>Delegates to {@link #getLayoutStrategy()}.
	 */
	public String resolvePackage(String pattern, String resourceName) {
		return getLayoutStrategy().resolvePackage(pattern, resourceName);
	}
}
