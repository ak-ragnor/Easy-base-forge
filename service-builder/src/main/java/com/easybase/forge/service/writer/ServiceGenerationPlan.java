package com.easybase.forge.service.writer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.io.GenerationUnit;
import com.easybase.forge.service.config.ServiceConfig;
import com.easybase.forge.service.generator.ServiceArtifactGenerator;
import com.easybase.forge.service.generator.entity.EntityGenerator;
import com.easybase.forge.service.generator.hook.HookBaseGenerator;
import com.easybase.forge.service.generator.hook.HookGenerator;
import com.easybase.forge.service.generator.model.ModelBaseGenerator;
import com.easybase.forge.service.generator.model.ModelGenerator;
import com.easybase.forge.service.generator.persistence.JpaRepositoryBaseGenerator;
import com.easybase.forge.service.generator.persistence.JpaRepositoryGenerator;
import com.easybase.forge.service.generator.persistence.PersistenceAdapterBaseGenerator;
import com.easybase.forge.service.generator.persistence.PersistenceAdapterGenerator;
import com.easybase.forge.service.generator.repository.RepositoryBaseGenerator;
import com.easybase.forge.service.generator.repository.RepositoryGenerator;
import com.easybase.forge.service.generator.service.LocalServiceBaseGenerator;
import com.easybase.forge.service.generator.service.LocalServiceBaseImplGenerator;
import com.easybase.forge.service.generator.service.LocalServiceGenerator;
import com.easybase.forge.service.generator.service.ServiceBaseGenerator;
import com.easybase.forge.service.generator.service.ServiceBaseImplGenerator;
import com.easybase.forge.service.generator.service.ServiceGenerator;

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
