package com.easybase.forge.core.writer;

import com.easybase.forge.core.config.*;
import com.easybase.forge.core.engine.GeneratorEngine;
import com.easybase.forge.core.model.*;
import com.easybase.forge.core.parser.OpenApiLoader;
import com.easybase.forge.core.parser.PaginationDetector;
import com.easybase.forge.core.parser.ResourceExtractor;
import com.easybase.forge.core.parser.SchemaResolver;
import com.easybase.forge.core.parser.ValidationMapper;
import com.easybase.forge.core.writer.GenerationPlan;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies flat-layout conflict detection and correct package resolution.
 */
class FlatLayoutConflictTest {

    @TempDir
    Path outputDir;

    private GeneratorEngine engine(LayoutMode layout) throws Exception {
        GeneratorConfig config = new GeneratorConfig();
        config.setBasePackage("com.example.api");
        OutputConfig output = new OutputConfig();
        output.setLayout(layout);
        config.setOutput(output);
        config.withOutputDirectory(outputDir);
        return new GeneratorEngine(config);
    }

    private Path specPath() throws Exception {
        URL url = getClass().getResource("/specs/petstore.yaml");
        assertThat(url).as("petstore.yaml not found").isNotNull();
        return Paths.get(url.toURI());
    }

    @Test
    void multiModule_eachResourceGetsOwnSubDirectory() throws Exception {
        engine(LayoutMode.MULTI_MODULE).generate(specPath());

        // In multi-module layout, each resource has its own sub-directory
        assertThat(outputDir.resolve("com/example/api/pets/dto")).isDirectory();
        assertThat(outputDir.resolve("com/example/api/orders/dto")).isDirectory();
    }

    @Test
    void flat_allArtifactsLandInSharedPackages() throws Exception {
        engine(LayoutMode.FLAT).generate(specPath());

        // Flat layout collapses all {resource} segments
        assertThat(outputDir.resolve("com/example/api/dto")).isDirectory();
        assertThat(outputDir.resolve("com/example/api/delegate")).isDirectory();
        assertThat(outputDir.resolve("com/example/api/controller/base")).isDirectory();

        // No per-resource sub-directories
        assertThat(outputDir.resolve("com/example/api/pets")).doesNotExist();
        assertThat(outputDir.resolve("com/example/api/orders")).doesNotExist();
    }

    @Test
    void flat_conflictingDtoNames_throwsConfigException() throws Exception {
        GeneratorConfig config = new GeneratorConfig();
        config.setBasePackage("com.example.api");
        OutputConfig output = new OutputConfig();
        output.setLayout(LayoutMode.FLAT);
        config.setOutput(output);
        config.withOutputDirectory(outputDir);

        // Build two resources that each have a DTO named "SharedDTO"
        DtoSchema sharedDto = DtoSchema.of("SharedDTO", "com.example.api.dto", List.of());
        ApiResource resource1 = new ApiResource("Pets", "pets", List.of(), List.of(sharedDto));
        ApiResource resource2 = new ApiResource("Orders", "orders", List.of(), List.of(sharedDto));

        GenerationPlan plan = new GenerationPlan();

        assertThatThrownBy(() -> plan.build(List.of(resource1, resource2), config))
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("Flat layout conflict")
                .hasMessageContaining("SharedDTO");
    }
}
