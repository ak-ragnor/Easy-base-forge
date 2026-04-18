package com.easybase.forge.core.service.writer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.easybase.forge.core.service.generator.EntityGenerator;
import com.easybase.forge.core.service.generator.HookBaseGenerator;
import com.easybase.forge.core.service.generator.HookGenerator;
import com.easybase.forge.core.service.generator.JpaRepositoryBaseGenerator;
import com.easybase.forge.core.service.generator.JpaRepositoryGenerator;
import com.easybase.forge.core.service.generator.LocalServiceBaseGenerator;
import com.easybase.forge.core.service.generator.LocalServiceBaseImplGenerator;
import com.easybase.forge.core.service.generator.LocalServiceGenerator;
import com.easybase.forge.core.service.generator.ModelBaseGenerator;
import com.easybase.forge.core.service.generator.ModelGenerator;
import com.easybase.forge.core.service.generator.PersistenceAdapterBaseGenerator;
import com.easybase.forge.core.service.generator.PersistenceAdapterGenerator;
import com.easybase.forge.core.service.generator.RepositoryBaseGenerator;
import com.easybase.forge.core.service.generator.RepositoryGenerator;
import com.easybase.forge.core.service.generator.ServiceArtifactGenerator;
import com.easybase.forge.core.service.generator.ServiceBaseGenerator;
import com.easybase.forge.core.service.generator.ServiceBaseImplGenerator;
import com.easybase.forge.core.service.generator.ServiceGenerator;
import com.easybase.forge.core.writer.GenerationUnit;

public class ServiceGenerationPlan {

	private final List<ServiceArtifactGenerator> generators;

	public ServiceGenerationPlan() {
		this(List.of(
				new ModelBaseGenerator(),
				new ModelGenerator(),
				new EntityGenerator(),
				new RepositoryBaseGenerator(),
				new RepositoryGenerator(),
				new JpaRepositoryBaseGenerator(),
				new JpaRepositoryGenerator(),
				new PersistenceAdapterBaseGenerator(),
				new PersistenceAdapterGenerator(),
				new HookBaseGenerator(),
				new HookGenerator(),
				new ServiceBaseGenerator(),
				new ServiceBaseImplGenerator(),
				new LocalServiceBaseGenerator(),
				new LocalServiceBaseImplGenerator(),
				new LocalServiceGenerator(),
				new ServiceGenerator()));
	}

	ServiceGenerationPlan(List<ServiceArtifactGenerator> generators) {
		this.generators = List.copyOf(generators);
	}

	public List<GenerationUnit> build(ServiceConfig config) {
		List<GenerationUnit> units = new ArrayList<>();
		Set<String> scheduledPaths = new HashSet<>();

		for (ServiceArtifactGenerator generator : generators) {
			for (GeneratedArtifact artifact : generator.generate(config)) {
				String path = artifact.outputPath().toString();

				if (!scheduledPaths.add(path)) {
					continue;
				}

				boolean overwrite = artifact.artifactType().shouldAlwaysOverwrite();
				units.add(new GenerationUnit(artifact, overwrite));
			}
		}

		return units;
	}
}
