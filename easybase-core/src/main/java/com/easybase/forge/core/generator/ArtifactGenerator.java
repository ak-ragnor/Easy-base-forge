package com.easybase.forge.core.generator;

import java.util.List;

import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.model.ApiResource;

/**
 * Common contract for all artifact generators.
 *
 * <p>Each generator produces zero or more {@link GeneratedArtifact}s for one
 * {@link ApiResource}. Returning an empty list is valid (e.g. when a feature
 * is disabled in the config).
 *
 * <p>Implementations must be stateless: the same instance may be called concurrently
 * for different resources.
 */
public interface ArtifactGenerator {

	/**
	 * Generates artifacts for the given resource using the supplied config.
	 *
	 * @param resource the resource to generate artifacts for
	 * @param config   the generator configuration
	 * @return a (possibly empty) list of generated artifacts; never {@code null}
	 */
	List<GeneratedArtifact> generate(ApiResource resource, GeneratorConfig config);
}
