package com.easybase.forge.core.engine;

import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.config.LayoutMode;
import com.easybase.forge.core.config.OutputConfig;
import com.easybase.forge.core.writer.GenerationReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test: runs the full parse → plan → generate → write pipeline
 * against the petstore fixture and verifies the output files exist and contain
 * the expected code structures.
 */
class GeneratorEngineIntegrationTest {

    @TempDir
    Path outputDir;

    private GeneratorEngine engine;
    private Path specPath;

    @BeforeEach
    void setUp() {
        URL specUrl = getClass().getResource("/specs/petstore.yaml");
        assertThat(specUrl).as("petstore.yaml not found").isNotNull();
        specPath = Paths.get(specUrl.getPath());

        GeneratorConfig config = new GeneratorConfig();
        config.setBasePackage("com.example.api");
        OutputConfig output = new OutputConfig();
        output.setDirectory(outputDir.toString());
        output.setLayout(LayoutMode.MULTI_MODULE);
        config.setOutput(output);
        config.withOutputDirectory(outputDir);

        engine = new GeneratorEngine(config);
    }

    @Test
    void generateCreatesExpectedFiles() {
        GenerationReport report = engine.generate(specPath);

        assertThat(report.hasErrors()).as("Errors: " + report.errorSummary()).isFalse();
        // 5 DTOs (PetDTO, CreatePetRequest, UpdatePetRequest, OrderDTO, CreateOrderRequest)
        // + 2 delegates + 2 base controllers + 2 custom controllers = 11 files minimum
        int totalCreated = report.created().size();
        assertThat(totalCreated).isGreaterThanOrEqualTo(11);
    }

    @Test
    void petsDelegateContainsAllEndpoints() throws IOException {
        engine.generate(specPath);
        String delegate = readFile("com/example/api/pets/delegate/PetsApiDelegate.java");

        assertThat(delegate).contains("interface PetsApiDelegate");
        assertThat(delegate).contains("listPets(");
        assertThat(delegate).contains("createPet(");
        assertThat(delegate).contains("getPetById(");
        assertThat(delegate).contains("updatePet(");
        assertThat(delegate).contains("deletePet(");
        assertThat(delegate).contains("ResponseEntity");
    }

    @Test
    void petsBaseControllerContainsSpringAnnotations() throws IOException {
        engine.generate(specPath);
        String base = readFile("com/example/api/pets/controller/base/PetsControllerBase.java");

        assertThat(base).contains("abstract class PetsControllerBase");
        assertThat(base).contains("@GetMapping");
        assertThat(base).contains("@PostMapping");
        assertThat(base).contains("@DeleteMapping");
        assertThat(base).contains("@PathVariable");
        assertThat(base).contains("@RequestBody");
        assertThat(base).contains("delegate.");
    }

    @Test
    void petsCustomControllerExtendsBase() throws IOException {
        engine.generate(specPath);
        String ctrl = readFile("com/example/api/pets/controller/PetsController.java");

        assertThat(ctrl).contains("@RestController");
        assertThat(ctrl).contains("extends PetsControllerBase");
        assertThat(ctrl).contains("super(delegate)");
    }

    @Test
    void createPetRequestDtoHasLombokAndValidation() throws IOException {
        engine.generate(specPath);
        String dto = readFile("com/example/api/pets/dto/CreatePetRequest.java");

        assertThat(dto).contains("@Data");
        assertThat(dto).contains("@NotBlank");
        assertThat(dto).contains("@Size");
        assertThat(dto).contains("@JsonProperty");
        assertThat(dto).contains("private String name");
    }

    @Test
    void petDtoHasCorrectTypeForTimestamp() throws IOException {
        engine.generate(specPath);
        String dto = readFile("com/example/api/pets/dto/PetDTO.java");

        assertThat(dto).contains("OffsetDateTime");
        assertThat(dto).contains("UUID");
    }

    @Test
    void orderDtoHasLocalDate() throws IOException {
        engine.generate(specPath);
        String dto = readFile("com/example/api/orders/dto/OrderDTO.java");

        assertThat(dto).contains("LocalDate");
    }

    @Test
    void regenerationSkipsExistingCustomController() throws IOException {
        // First run — creates everything
        GenerationReport first = engine.generate(specPath);
        assertThat(first.created()).anyMatch(p -> p.contains("PetsController.java"));

        // Tamper with the custom controller to simulate user edits
        Path customCtrl = outputDir.resolve("com/example/api/pets/controller/PetsController.java");
        String original = Files.readString(customCtrl);
        Files.writeString(customCtrl, original + "\n// user edit");

        // Second run — base controller and DTOs updated, custom controller skipped
        GenerationReport second = engine.generate(specPath);
        assertThat(second.skipped()).anyMatch(p -> p.contains("PetsController.java"));
        assertThat(second.updated()).anyMatch(p -> p.contains("PetsControllerBase.java"));

        // User edit must be preserved
        String afterRegen = Files.readString(customCtrl);
        assertThat(afterRegen).contains("// user edit");
    }

    @Test
    void ordersDelegateAndBaseControllerAreGenerated() throws IOException {
        engine.generate(specPath);

        String delegate = readFile("com/example/api/orders/delegate/OrdersApiDelegate.java");
        assertThat(delegate).contains("interface OrdersApiDelegate");
        assertThat(delegate).contains("createOrder(");
        assertThat(delegate).contains("getOrderById(");

        String base = readFile("com/example/api/orders/controller/base/OrdersControllerBase.java");
        assertThat(base).contains("abstract class OrdersControllerBase");
    }

    private String readFile(String relativePath) throws IOException {
        Path file = outputDir.resolve(relativePath);
        assertThat(file).as("Expected file not found: " + relativePath).exists();
        return Files.readString(file);
    }
}
