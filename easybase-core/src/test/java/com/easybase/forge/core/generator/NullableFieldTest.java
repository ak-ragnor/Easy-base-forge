package com.easybase.forge.core.generator;

import com.easybase.forge.core.config.*;
import com.easybase.forge.core.engine.GeneratorEngine;
import com.easybase.forge.core.writer.GenerationReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that nullable: true OpenAPI fields get @Nullable in generated DTOs.
 */
class NullableFieldTest {

    @TempDir
    Path outputDir;

    private GeneratorEngine engine() {
        GeneratorConfig config = new GeneratorConfig();
        config.setBasePackage("com.example.api");
        config.withOutputDirectory(outputDir);
        return new GeneratorEngine(config);
    }

    private Path specPath() throws Exception {
        URL url = getClass().getResource("/specs/animals.yaml");
        assertThat(url).as("animals.yaml not found").isNotNull();
        return Paths.get(url.toURI());
    }

    @Test
    void nullableField_getsNullableAnnotation() throws Exception {
        engine().generate(specPath());

        // Cat.indoor and Dog.breed are nullable: true in the spec
        Path catFile = outputDir.resolve("com/example/api/animals/dto/Cat.java");
        assertThat(catFile).exists();

        String catContent = Files.readString(catFile);
        // @Nullable should appear before the indoor field
        assertThat(catContent).contains("@Nullable");
        assertThat(catContent).containsPattern("@Nullable[\\s\\S]*?indoor");
    }

    @Test
    void nonNullableField_noNullableAnnotation() throws Exception {
        engine().generate(specPath());

        // Cat.name is required (not nullable) — should not have @Nullable
        Path catFile = outputDir.resolve("com/example/api/animals/dto/Cat.java");
        String catContent = Files.readString(catFile);

        // Verify @Nullable is present at least once (for indoor)
        assertThat(catContent).contains("@Nullable");

        // Verify 'name' field does NOT have @Nullable directly before it
        // (The pattern @Nullable\n  private String name should not appear)
        assertThat(catContent).doesNotContainPattern("@Nullable[\\s\\S]{0,30}private String name");
    }

    @Test
    void nullableImportPresent() throws Exception {
        engine().generate(specPath());

        Path catFile = outputDir.resolve("com/example/api/animals/dto/Cat.java");
        String content = Files.readString(catFile);
        assertThat(content).contains("import org.springframework.lang.Nullable");
    }
}
