package com.easybase.forge.core.generator;

import com.easybase.forge.core.config.*;
import com.easybase.forge.core.engine.GeneratorEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.easybase.forge.core.config.ConfigException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies that transitively referenced schemas (nested $ref fields) are collected
 * and generated even when the root schema was pre-registered from components.
 *
 * <p>This covers the bug where CategoryDTO and AddressDTO were missing because
 * SchemaResolver.ensureDtoRegistered() returned early for pre-registered schemas,
 * skipping transitive field tracking.
 */
class TransitiveSchemaTest {

    @TempDir
    Path outputDir;

    private GeneratorEngine engine(LayoutMode layout) {
        GeneratorConfig config = new GeneratorConfig();
        config.setBasePackage("com.example.api");

        OutputConfig output = new OutputConfig();
        output.setLayout(layout);
        config.setOutput(output);

        config.withOutputDirectory(outputDir);
        return new GeneratorEngine(config);
    }

    private Path specPath() throws Exception {
        URL url = getClass().getResource("/specs/nested.yaml");
        assertThat(url).as("nested.yaml not found").isNotNull();
        return Paths.get(url.toURI());
    }

    @Test
    void flat_transitiveRef_categoryDtoGenerated() throws Exception {
        engine(LayoutMode.FLAT).generate(specPath());

        // CategoryDTO is referenced transitively via PetDTO.category — must be generated
        assertThat(outputDir.resolve("com/example/api/dto/CategoryDTO.java")).exists();
    }

    @Test
    void flat_deepTransitiveRef_addressDtoGenerated() throws Exception {
        engine(LayoutMode.FLAT).generate(specPath());

        // AddressDTO is referenced transitively via OrderDTO.shippingAddress — must be generated
        assertThat(outputDir.resolve("com/example/api/dto/AddressDTO.java")).exists();
    }

    @Test
    void flat_sharedTransitiveRef_generatedOnce() throws Exception {
        engine(LayoutMode.FLAT).generate(specPath());

        // CategoryDTO is referenced by both pets and orders — should appear exactly once
        long count = Files.list(outputDir.resolve("com/example/api/dto"))
                .filter(p -> p.getFileName().toString().equals("CategoryDTO.java"))
                .count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void multiModule_sharedTransitiveRef_throwsConflictError() {
        // nested.yaml: CategoryDTO is referenced by both pets and orders tags.
        // In MULTI_MODULE, shared schemas are forbidden — conflict must be detected.
        GeneratorEngine e = engine(LayoutMode.MULTI_MODULE);
        assertThatThrownBy(() -> e.generate(specPath()))
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("MULTI_MODULE layout")
                .hasMessageContaining("CategoryDTO");
    }
}
