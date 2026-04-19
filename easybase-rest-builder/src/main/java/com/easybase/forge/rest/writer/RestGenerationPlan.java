package com.easybase.forge.rest.writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.easybase.forge.common.config.ConfigException;
import com.easybase.forge.common.config.GeneratorConfig;
import com.easybase.forge.common.config.LayoutMode;
import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.io.GenerationUnit;
import com.easybase.forge.rest.generator.RestArtifactGenerator;
import com.easybase.forge.rest.generator.controller.BaseControllerGenerator;
import com.easybase.forge.rest.generator.controller.CustomControllerGenerator;
import com.easybase.forge.rest.generator.delegate.DelegateGenerator;
import com.easybase.forge.rest.generator.delegate.DelegateImplGenerator;
import com.easybase.forge.rest.generator.dto.DtoGenerator;
import com.easybase.forge.rest.model.ApiResource;
import com.easybase.forge.rest.model.DtoSchema;

public class RestGenerationPlan {

	private final List<RestArtifactGenerator> generators;

	public RestGenerationPlan() {
		this(List.of(
				new DtoGenerator(),
				new DelegateGenerator(),
				new DelegateImplGenerator(),
				new BaseControllerGenerator(),
				new CustomControllerGenerator()));
	}

	RestGenerationPlan(List<RestArtifactGenerator> generators) {
		this.generators = List.copyOf(generators);
	}

	public List<GenerationUnit> build(List<ApiResource> resources, GeneratorConfig config) {
		List<GenerationUnit> units = new ArrayList<>();

		if (config.getLayoutStrategy().mode() == LayoutMode.MULTI_MODULE) {
			checkMultiModuleConflicts(resources);
		}

		Set<String> scheduledPaths = new HashSet<>();

		for (ApiResource resource : resources) {
			for (RestArtifactGenerator generator : generators) {
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
