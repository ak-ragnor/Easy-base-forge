package com.easybase.forge.core.service.generator;

import java.util.List;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.service.config.ServiceConfig;

/**
 * Contract for all Service Builder artifact generators.
 *
 * <p>Each generator is responsible for producing zero or more {@link GeneratedArtifact}s
 * for the given entity configuration. Returning an empty list is valid (e.g. when a
 * feature is disabled, such as hooks).
 *
 * <p>Implementations must be stateless: the same instance may be reused across multiple
 * generation runs.
 *
 * <p>This interface is intentionally separate from
 * {@link com.easybase.forge.core.generator.ArtifactGenerator}, which operates on
 * {@link com.easybase.forge.core.model.ApiResource} objects parsed from OpenAPI specs.
 */
public interface ServiceArtifactGenerator {

	/**
	 * Generates artifacts for the given entity service configuration.
	 *
	 * @param config the fully-loaded and validated service configuration
	 * @return a (possibly empty) list of generated artifacts; never {@code null}
	 */
	List<GeneratedArtifact> generate(ServiceConfig config);
}
