package com.easybase.forge.core.generator;

import com.easybase.forge.core.config.*;
import com.easybase.forge.core.engine.GeneratorEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ResponseEntityMode.NEVER and VOID_ONLY on the generated delegate and base controller.
 */
class ResponseEntityModeTest {

    @TempDir
    Path outputDir;

    private GeneratorEngine engine(ResponseEntityMode mode) {
        GeneratorConfig config = new GeneratorConfig();
        config.setBasePackage("com.example.api");

        GenerateOptions opts = new GenerateOptions();
        opts.setResponseEntityWrapping(mode);
        config.setGenerate(opts);

        OutputConfig output = new OutputConfig();
        output.setLayout(LayoutMode.MULTI_MODULE);
        config.setOutput(output);

        config.withOutputDirectory(outputDir);
        return new GeneratorEngine(config);
    }

    private Path specPath() throws Exception {
        URL url = getClass().getResource("/specs/petstore.yaml");
        assertThat(url).as("petstore.yaml not found").isNotNull();
        return Paths.get(url.toURI());
    }

    // ── NEVER ────────────────────────────────────────────────────────────────

    @Test
    void never_delegateReturnsPlainType() throws Exception {
        engine(ResponseEntityMode.NEVER).generate(specPath());

        String content = Files.readString(
                outputDir.resolve("com/example/api/pets/delegate/PetsApiDelegate.java"));

        // Non-void methods return the plain type
        assertThat(content).contains("List<PetDTO> listPets(");
        assertThat(content).contains("PetDTO createPet(");
        assertThat(content).contains("PetDTO getPetById(");

        // No ResponseEntity wrapping
        assertThat(content).doesNotContain("ResponseEntity");
    }

    @Test
    void never_voidMethodReturnsVoid() throws Exception {
        engine(ResponseEntityMode.NEVER).generate(specPath());

        String content = Files.readString(
                outputDir.resolve("com/example/api/pets/delegate/PetsApiDelegate.java"));

        // deletePet returns 204 No Content → void
        assertThat(content).contains("void deletePet(");
    }

    @Test
    void never_baseControllerDelegatesDirectly() throws Exception {
        engine(ResponseEntityMode.NEVER).generate(specPath());

        String content = Files.readString(
                outputDir.resolve("com/example/api/pets/controller/base/PetsControllerBase.java"));

        assertThat(content).doesNotContain("ResponseEntity");
        assertThat(content).contains("return delegate.listPets(");
    }

    // ── VOID_ONLY ────────────────────────────────────────────────────────────

    @Test
    void voidOnly_nonVoidMethodReturnsPlainType() throws Exception {
        engine(ResponseEntityMode.VOID_ONLY).generate(specPath());

        String content = Files.readString(
                outputDir.resolve("com/example/api/pets/delegate/PetsApiDelegate.java"));

        // Methods with a response body return the plain type (no ResponseEntity wrapper)
        assertThat(content).contains("List<PetDTO> listPets(");
        assertThat(content).contains("PetDTO createPet(");
    }

    @Test
    void voidOnly_voidMethodReturnsResponseEntityVoid() throws Exception {
        engine(ResponseEntityMode.VOID_ONLY).generate(specPath());

        String content = Files.readString(
                outputDir.resolve("com/example/api/pets/delegate/PetsApiDelegate.java"));

        // deletePet is void → gets ResponseEntity<Void>
        assertThat(content).contains("ResponseEntity<Void> deletePet(");
    }

    // ── ALWAYS (sanity check, already covered elsewhere) ────────────────────

    @Test
    void always_allMethodsReturnResponseEntity() throws Exception {
        engine(ResponseEntityMode.ALWAYS).generate(specPath());

        String content = Files.readString(
                outputDir.resolve("com/example/api/pets/delegate/PetsApiDelegate.java"));

        assertThat(content).contains("ResponseEntity<List<PetDTO>> listPets(");
        assertThat(content).contains("ResponseEntity<PetDTO> createPet(");
        assertThat(content).contains("ResponseEntity<Void> deletePet(");
    }
}
