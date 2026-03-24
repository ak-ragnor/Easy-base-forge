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
 * Verifies oneOf (with discriminator) and anyOf (without discriminator) code generation.
 */
class OneOfAnyOfTest {

    @TempDir
    Path outputDir;

    private GeneratorEngine engine() {
        GeneratorConfig config = new GeneratorConfig();
        config.setBasePackage("com.example.api");

        OutputConfig output = new OutputConfig();
        output.setLayout(LayoutMode.MULTI_MODULE);
        config.setOutput(output);

        config.withOutputDirectory(outputDir);
        return new GeneratorEngine(config);
    }

    private Path specPath() throws Exception {
        URL url = getClass().getResource("/specs/animals.yaml");
        assertThat(url).as("animals.yaml not found").isNotNull();
        return Paths.get(url.toURI());
    }

    // ── discriminated oneOf ──────────────────────────────────────────────────

    @Test
    void discriminatedOneOf_abstractBaseGenerated() throws Exception {
        engine().generate(specPath());

        Path animalFile = outputDir.resolve("com/example/api/animals/dto/Animal.java");
        assertThat(animalFile).exists();

        String content = Files.readString(animalFile);
        assertThat(content).contains("abstract class Animal");
        assertThat(content).contains("@JsonTypeInfo");
        assertThat(content).contains("property = \"type\"");
        assertThat(content).contains("@JsonSubTypes");
        assertThat(content).contains("Cat.class");
        assertThat(content).contains("Dog.class");
        assertThat(content).contains("\"cat\"");
        assertThat(content).contains("\"dog\"");
    }

    @Test
    void discriminatedOneOf_variantsExtendBase() throws Exception {
        engine().generate(specPath());

        Path catFile = outputDir.resolve("com/example/api/animals/dto/Cat.java");
        Path dogFile = outputDir.resolve("com/example/api/animals/dto/Dog.java");
        assertThat(catFile).exists();
        assertThat(dogFile).exists();

        assertThat(Files.readString(catFile)).contains("class Cat extends Animal");
        assertThat(Files.readString(dogFile)).contains("class Dog extends Animal");
    }

    @Test
    void discriminatedOneOf_variantsHaveOwnFields() throws Exception {
        engine().generate(specPath());

        String catContent = Files.readString(outputDir.resolve("com/example/api/animals/dto/Cat.java"));
        assertThat(catContent).contains("indoor");
        assertThat(catContent).contains("name");

        String dogContent = Files.readString(outputDir.resolve("com/example/api/animals/dto/Dog.java"));
        assertThat(dogContent).contains("breed");
        assertThat(dogContent).contains("name");
    }

    @Test
    void discriminatedOneOf_delegateUsesSchemaNameAsReturnType() throws Exception {
        engine().generate(specPath());

        Path delegate = outputDir.resolve("com/example/api/animals/delegate/AnimalsApiDelegate.java");
        assertThat(delegate).exists();

        String content = Files.readString(delegate);
        // The method returning an Animal uses the schema name directly (no "Base" suffix)
        assertThat(content).contains("Animal");
        assertThat(content).doesNotContain("AnimalBase");
    }

    // ── anyOf without discriminator ──────────────────────────────────────────

    @Test
    void anyOfNoDiscriminator_variantsStillGenerated() throws Exception {
        engine().generate(specPath());

        // Both ClickEvent and HoverEvent should still be generated as DTOs
        Path clickEvent = outputDir.resolve("com/example/api/events/dto/ClickEvent.java");
        Path hoverEvent = outputDir.resolve("com/example/api/events/dto/HoverEvent.java");
        assertThat(clickEvent).exists();
        assertThat(hoverEvent).exists();

        assertThat(Files.readString(clickEvent)).contains("class ClickEvent");
        assertThat(Files.readString(hoverEvent)).contains("class HoverEvent");
    }

    @Test
    void anyOfNoDiscriminator_returnTypeIsObject() throws Exception {
        engine().generate(specPath());

        Path delegate = outputDir.resolve("com/example/api/events/delegate/EventsApiDelegate.java");
        assertThat(delegate).exists();

        String content = Files.readString(delegate);
        // No discriminator → falls back to Object
        assertThat(content).contains("ResponseEntity<Object>");
    }
}
