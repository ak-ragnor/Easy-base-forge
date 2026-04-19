package com.easybase.forge.common.config;

import java.nio.file.Path;

import com.easybase.forge.common.config.layout.LayoutStrategy;
import com.easybase.forge.common.config.layout.LayoutStrategyFactory;

/**
 * Root project configuration loaded from {@code easybase-config.yaml}.
 *
 * <p>Shared by both REST Builder and Service Builder: REST uses it directly for
 * generation, while Service Builder reads it for package defaults, authors, and
 * structure overrides.
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

	public LayoutStrategy getLayoutStrategy() {
		if (layoutStrategy == null) {
			LayoutMode mode = LayoutMode.FLAT;

			if (output != null && output.getLayout() != null) {
				mode = output.getLayout();
			}

			layoutStrategy = LayoutStrategyFactory.create(mode, basePackage);
		}

		return layoutStrategy;
	}

	public String resolvePackage(String pattern, String resourceName) {
		return getLayoutStrategy().resolvePackage(pattern, resourceName);
	}
}
