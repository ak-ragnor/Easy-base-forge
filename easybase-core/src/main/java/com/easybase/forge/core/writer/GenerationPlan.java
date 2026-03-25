package com.easybase.forge.core.writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.easybase.forge.core.config.ConfigException;
import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.config.LayoutMode;
import com.easybase.forge.core.generator.ArtifactGenerator;
import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.generator.controller.BaseControllerGenerator;
import com.easybase.forge.core.generator.controller.CustomControllerGenerator;
import com.easybase.forge.core.generator.delegate.DelegateGenerator;
import com.easybase.forge.core.generator.delegate.DelegateImplGenerator;
import com.easybase.forge.core.generator.dto.DtoGenerator;
import com.easybase.forge.core.model.ApiResource;
import com.easybase.forge.core.model.DtoSchema;

/**
 * Builds the list of {@link GenerationUnit}s for a full generation run.
 *
 * <p>This is where regeneration-safety is decided: the overwrite flag on each unit
 * is derived from {@link com.easybase.forge.core.model.ArtifactType#shouldAlwaysOverwrite()}.
 * Artifacts that are always overwritten are unconditionally regenerated; user-owned artifacts
 * ({@code CUSTOM_CONTROLLER}, {@code DELEGATE_IMPL}) are only created on first run.
 *
 * <table>
 *   <tr><th>Artifact type</th><th>Overwrite behaviour</th></tr>
 *   <tr><td>BASE_CONTROLLER</td><td>Always overwrite</td></tr>
 *   <tr><td>DELEGATE</td><td>Always overwrite</td></tr>
 *   <tr><td>DTO</td><td>Always overwrite</td></tr>
 *   <tr><td>DELEGATE_IMPL_BASE</td><td>Always overwrite</td></tr>
 *   <tr><td>CUSTOM_CONTROLLER</td><td>Only create; never overwrite existing</td></tr>
 *   <tr><td>DELEGATE_IMPL</td><td>Only create; never overwrite existing</td></tr>
 * </table>
 */
public class GenerationPlan {

	private final List<ArtifactGenerator> generators;

	/**
	 * Creates a plan with the default set of generators.
	 */
	public GenerationPlan() {
		this(List.of(
				new DtoGenerator(),
				new DelegateGenerator(),
				new DelegateImplGenerator(),
				new BaseControllerGenerator(),
				new CustomControllerGenerator()));
	}

	/**
	 * Creates a plan with a custom set of generators.
	 * Primarily used for testing.
	 *
	 * @param generators the generators to use, in the order they will be invoked
	 */
	GenerationPlan(List<ArtifactGenerator> generators) {
		this.generators = List.copyOf(generators);
	}

	public List<GenerationUnit> build(List<ApiResource> resources, GeneratorConfig config) {
		List<GenerationUnit> units = new ArrayList<>();

		if (config.getLayoutStrategy().mode() == LayoutMode.MULTI_MODULE) {
			checkMultiModuleConflicts(resources);
		}

		Set<String> scheduledPaths = new HashSet<>();

		for (ApiResource resource : resources) {
			for (ArtifactGenerator generator : generators) {
				for (GeneratedArtifact artifact : generator.generate(resource, config)) {
					String path = artifact.outputPath().toString();

					if (!scheduledPaths.add(path)) {
						continue;
					}

					boolean overwrite = artifact.artifactType().shouldAlwaysOverwrite();
					units.add(new GenerationUnit(artifact, overwrite));
				}
			}
		}

		return units;
	}

	/**
	 * Verifies that no two resources share the same DTO schema name in MULTI_MODULE layout.
	 *
	 * <p>MULTI_MODULE generates a separate DTO package per resource, so a schema referenced
	 * by two tags would produce two incompatible classes in different packages.
	 * Users should switch to FLAT layout to share schemas across tags.
	 *
	 * @throws ConfigException if a shared schema is detected
	 */
	private void checkMultiModuleConflicts(List<ApiResource> resources) {
		Map<String, String> seen = new HashMap<>();

		for (ApiResource resource : resources) {
			for (DtoSchema schema : resource.dtoSchemas()) {
				String existing = seen.put(schema.className(), resource.name());

				if (existing != null) {
					throw new ConfigException("MULTI_MODULE layout: schema '" + schema.className() + "' is shared by "
							+ "resources '" + existing + "' and '" + resource.name() + "'. "
							+ "MULTI_MODULE generates a separate DTO package per resource, so "
							+ "schemas cannot be shared across tags. "
							+ "Use 'output.layout: FLAT' instead.");
				}
			}
		}
	}
}
