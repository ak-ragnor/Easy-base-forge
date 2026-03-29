package com.easybase.forge.core.service.writer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.easybase.forge.core.service.generator.BaseEntityGenerator;
import com.easybase.forge.core.service.generator.BaseServiceGenerator;
import com.easybase.forge.core.service.generator.BaseServiceImplGenerator;
import com.easybase.forge.core.service.generator.EntityGenerator;
import com.easybase.forge.core.service.generator.HookGenerator;
import com.easybase.forge.core.service.generator.HookImplGenerator;
import com.easybase.forge.core.service.generator.JpaRepositoryGenerator;
import com.easybase.forge.core.service.generator.ModelGenerator;
import com.easybase.forge.core.service.generator.PersistenceAdapterGenerator;
import com.easybase.forge.core.service.generator.RepositoryGenerator;
import com.easybase.forge.core.service.generator.ServiceArtifactGenerator;
import com.easybase.forge.core.service.generator.UserServiceGenerator;
import com.easybase.forge.core.service.generator.UserServiceImplGenerator;
import com.easybase.forge.core.writer.GenerationUnit;

/**
 * Builds the ordered list of {@link GenerationUnit}s for a Service Builder run.
 *
 * <p>Applies the same regeneration-safety semantics as the REST builder's
 * {@link com.easybase.forge.core.writer.GenerationPlan}:
 * artifacts whose {@code ArtifactType.shouldAlwaysOverwrite()} returns {@code true}
 * are unconditionally regenerated; user-owned artifacts are only created once.
 *
 * <p>Generators are invoked in a fixed order that ensures dependencies exist on disk
 * before the files that reference them are written:
 * <ol>
 *   <li>Domain model</li>
 *   <li>Repository interface</li>
 *   <li>JPA infrastructure (BaseEntity, Entity, JpaRepository, PersistenceAdapter)</li>
 *   <li>Service layer (BaseService, BaseServiceImpl, UserService, UserServiceImpl)</li>
 *   <li>Hook infrastructure (Hook, HookImpl)</li>
 * </ol>
 */
public class ServiceGenerationPlan {

	private final List<ServiceArtifactGenerator> generators;

	/**
	 * Creates a plan with the default set of service generators.
	 */
	public ServiceGenerationPlan() {
		this(List.of(
				new ModelGenerator(),
				new RepositoryGenerator(),
				new BaseEntityGenerator(),
				new EntityGenerator(),
				new JpaRepositoryGenerator(),
				new PersistenceAdapterGenerator(),
				new BaseServiceGenerator(),
				new BaseServiceImplGenerator(),
				new UserServiceGenerator(),
				new UserServiceImplGenerator(),
				new HookGenerator(),
				new HookImplGenerator()));
	}

	/**
	 * Creates a plan with a custom set of generators.
	 * Primarily used for testing.
	 *
	 * @param generators the generators to use, in the order they will be invoked
	 */
	ServiceGenerationPlan(List<ServiceArtifactGenerator> generators) {
		this.generators = List.copyOf(generators);
	}

	/**
	 * Builds the list of generation units from all generators.
	 *
	 * <p>Duplicate output paths are de-duplicated — first writer wins.
	 *
	 * @param config the fully-loaded entity configuration
	 * @return the ordered list of units ready for {@link com.easybase.forge.core.writer.ArtifactWriter}
	 */
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
