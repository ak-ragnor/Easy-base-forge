package com.easybase.forge.service.generator;

import java.util.List;

import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.service.config.ServiceConfig;

/**
 * Contract for all Service Builder artifact generators.
 *
 * <p>Each generator is responsible for producing zero or more {@link GeneratedArtifact}s
 * for the given entity configuration. Returning an empty list is valid (e.g. when a
 * feature is disabled, such as hooks).
 *
 * <p>Implementations must be stateless: the same instance may be reused across multiple
 * generation runs.
 */
public interface ServiceArtifactGenerator {

	List<GeneratedArtifact> generate(ServiceConfig config);
}
