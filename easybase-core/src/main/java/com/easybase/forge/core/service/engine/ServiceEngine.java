package com.easybase.forge.core.service.engine;

import java.util.List;

import com.easybase.forge.core.service.config.ServiceConfig;
import com.easybase.forge.core.service.writer.ServiceGenerationPlan;
import com.easybase.forge.core.writer.ArtifactWriter;
import com.easybase.forge.core.writer.GenerationReport;
import com.easybase.forge.core.writer.GenerationUnit;

/**
 * Top-level orchestrator for the Service Builder generation pipeline.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Build the ordered list of {@link GenerationUnit}s via {@link ServiceGenerationPlan}.</li>
 *   <li>Write all units to disk via {@link ArtifactWriter}
 *       (respects overwrite / skip-if-exists semantics).</li>
 *   <li>Return a {@link GenerationReport} for the caller to log or act on.</li>
 * </ol>
 *
 * <p>Example usage from the Maven Mojo or CLI command:
 * <pre>
 * ServiceConfig config = ServiceConfigLoader.load(projectConfig, entityConfig, outputDir);
 * GenerationReport report = new ServiceEngine(config).generate();
 * </pre>
 */
public class ServiceEngine {

	private final ServiceConfig config;

	/**
	 * Creates a new engine bound to the given configuration.
	 *
	 * @param config the fully-loaded and validated service configuration;
	 *               {@link ServiceConfig#getResolvedOutputDirectory()} must not be {@code null}
	 */
	public ServiceEngine(ServiceConfig config) {
		if (config == null) {
			throw new IllegalArgumentException("ServiceConfig must not be null");
		}

		if (config.getResolvedOutputDirectory() == null) {
			throw new IllegalArgumentException(
					"ServiceConfig.resolvedOutputDirectory must be set before calling generate()");
		}

		this.config = config;
	}

	/**
	 * Runs the full generation pipeline.
	 *
	 * @return a report containing lists of created, updated, skipped, and errored paths
	 */
	public GenerationReport generate() {
		List<GenerationUnit> units = new ServiceGenerationPlan().build(config);

		return new ArtifactWriter().write(units);
	}

	/**
	 * Returns the list of units that <em>would</em> be written without performing any I/O.
	 * Used for dry-run support in the CLI.
	 *
	 * @return the ordered list of planned generation units
	 */
	public List<GenerationUnit> plan() {
		return new ServiceGenerationPlan().build(config);
	}
}
