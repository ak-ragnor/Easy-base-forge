package com.easybase.forge.rest.engine;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import com.easybase.forge.common.config.GeneratorConfig;
import com.easybase.forge.common.io.ArtifactWriter;
import com.easybase.forge.common.io.GenerationReport;
import com.easybase.forge.common.io.GenerationUnit;
import com.easybase.forge.rest.model.ApiSpec;
import com.easybase.forge.rest.parser.OpenApiLoader;
import com.easybase.forge.rest.parser.PaginationDetector;
import com.easybase.forge.rest.parser.ResourceExtractor;
import com.easybase.forge.rest.parser.SchemaResolver;
import com.easybase.forge.rest.parser.ValidationMapper;
import com.easybase.forge.rest.writer.RestGenerationPlan;

import io.swagger.v3.oas.models.OpenAPI;

public class RestGeneratorEngine {

	private static final Logger LOG = Logger.getLogger(RestGeneratorEngine.class.getName());

	private final GeneratorConfig config;

	public RestGeneratorEngine(GeneratorConfig config) {
		this.config = config;
	}

	public ApiSpec parse(Path specFile) {
		OpenAPI openApi = new OpenApiLoader().load(specFile);
		return buildSpec(openApi);
	}

	public GenerationReport generate(Path specFile) {
		if (config.getResolvedOutputDirectory() == null) {
			throw new IllegalStateException("Output directory must be set before calling generate()");
		}

		OpenAPI openApi = new OpenApiLoader().load(specFile);
		ApiSpec spec = buildSpec(openApi);
		List<GenerationUnit> units = new RestGenerationPlan().build(spec.resources(), config);
		GenerationReport report = new ArtifactWriter().write(units);
		logReport(report);
		return report;
	}

	private ApiSpec buildSpec(OpenAPI openApi) {
		ValidationMapper validationMapper = new ValidationMapper();
		SchemaResolver schemaResolver = new SchemaResolver(openApi, validationMapper);
		PaginationDetector paginationDetector = new PaginationDetector();
		ResourceExtractor extractor = new ResourceExtractor(schemaResolver, validationMapper, paginationDetector);

		String title = "Unknown";
		String version = "0.0.0";

		if (openApi.getInfo() != null) {
			title = openApi.getInfo().getTitle();
			version = openApi.getInfo().getVersion();
		}

		return new ApiSpec(title, version, extractor.extract(openApi));
	}

	private static void logReport(GenerationReport report) {
		report.created().forEach(p -> LOG.info("[CREATED] " + p));
		report.updated().forEach(p -> LOG.info("[UPDATED] " + p));
		report.skipped().forEach(p -> LOG.fine("[SKIPPED] " + p));
		report.errors().forEach(e -> LOG.warning("[ERROR]   " + e));
	}
}
