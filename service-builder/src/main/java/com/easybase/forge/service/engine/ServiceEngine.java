package com.easybase.forge.service.engine;

import java.util.List;

import com.easybase.forge.common.io.ArtifactWriter;
import com.easybase.forge.common.io.GenerationReport;
import com.easybase.forge.common.io.GenerationUnit;
import com.easybase.forge.service.config.ServiceConfig;
import com.easybase.forge.service.writer.ServiceGenerationPlan;

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
 */
public class ServiceEngine {

	private final ServiceConfig config;

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

	public GenerationReport generate() {
		List<GenerationUnit> units = new ServiceGenerationPlan().build(config);
		return new ArtifactWriter().write(units);
	}

	public List<GenerationUnit> plan() {
		return new ServiceGenerationPlan().build(config);
	}
}
