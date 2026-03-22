package com.easybase.forge.core.engine;

import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.model.ApiResource;
import com.easybase.forge.core.model.ApiSpec;
import com.easybase.forge.core.parser.*;
import com.easybase.forge.core.writer.ArtifactWriter;
import com.easybase.forge.core.writer.GenerationPlan;
import com.easybase.forge.core.writer.GenerationReport;
import com.easybase.forge.core.writer.GenerationUnit;
import io.swagger.v3.oas.models.OpenAPI;

import java.nio.file.Path;
import java.util.List;

/**
 * Top-level orchestrator: config → parse → plan → generate → write.
 */
public class GeneratorEngine {

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

        List<ApiResource> resources = extractor.extract(openApi);
        String title = openApi.getInfo() != null ? openApi.getInfo().getTitle() : "Unknown";
        String version = openApi.getInfo() != null ? openApi.getInfo().getVersion() : "0.0.0";

        return new ApiSpec(title, version, resources);
    }

    private static void logReport(GenerationReport report) {
        report.created().forEach(p -> System.out.println("[CREATED] " + p));
        report.updated().forEach(p -> System.out.println("[UPDATED] " + p));
        report.skipped().forEach(p -> System.out.println("[SKIPPED] " + p));
        report.errors().forEach(e -> System.err.println("[ERROR]   " + e));
    }
}
