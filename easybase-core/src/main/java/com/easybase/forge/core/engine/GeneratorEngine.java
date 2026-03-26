package com.easybase.forge.core.engine;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.model.ApiResource;
import com.easybase.forge.core.model.ApiSpec;
import com.easybase.forge.core.parser.*;
import com.easybase.forge.core.writer.ArtifactWriter;
import com.easybase.forge.core.writer.GenerationPlan;
import com.easybase.forge.core.writer.GenerationReport;
import com.easybase.forge.core.writer.GenerationUnit;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * Top-level orchestrator: config → parse → plan → generate → write.
 */
public class GeneratorEngine {

	private static final Logger LOG = Logger.getLogger(GeneratorEngine.class.getName());

	private final GeneratorConfig config;

	public GeneratorEngine(GeneratorConfig config) {
		this.config = config;
	}

	/**
	 * Parses the given OpenAPI spec and returns the resolved domain model.
	 */
	public ApiSpec parse(Path specFile) {
		OpenAPI openApi = new OpenApiLoader().load(specFile);
		return buildSpec(openApi);
	}

	/**
	 * Full pipeline: parse → plan → generate → write.
	 *
	 * @param specFile path to the OpenAPI YAML/JSON spec
	 * @return report describing created, updated, skipped, and errored files
	 */
	public GenerationReport generate(Path specFile) {
		if (config.getResolvedOutputDirectory() == null) {
			throw new IllegalStateException("Output directory must be set before calling generate()");
		}

		OpenAPI openApi = new OpenApiLoader().load(specFile);

		ApiSpec spec = buildSpec(openApi);

		List<GenerationUnit> units = new GenerationPlan().build(spec.resources(), config);

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
