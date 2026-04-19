package com.easybase.forge.rest.generator;

import java.util.List;

import com.easybase.forge.common.config.GeneratorConfig;
import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.rest.model.ApiResource;

public interface RestArtifactGenerator {

	List<GeneratedArtifact> generate(ApiResource resource, GeneratorConfig config);
}
