package com.easybase.forge.core.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.easybase.forge.core.config.*;
import com.easybase.forge.core.engine.GeneratorEngine;

/**
 * Verifies that nullable: true OpenAPI fields get @Nullable in generated DTOs.
 */
class NullableFieldTest {

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

	@Test
	void nullableField_getsNullableAnnotation() throws Exception {
		engine().generate(specPath());

		Path catFile = outputDir.resolve("com/example/api/animals/dto/Cat.java");
		assertThat(catFile).exists();

		String catContent = Files.readString(catFile);

		assertThat(catContent).contains("@Nullable");
		assertThat(catContent).containsPattern("@Nullable[\\s\\S]*?indoor");
	}

	@Test
	void nonNullableField_noNullableAnnotation() throws Exception {
		engine().generate(specPath());

		Path catFile = outputDir.resolve("com/example/api/animals/dto/Cat.java");
		String catContent = Files.readString(catFile);

		assertThat(catContent).contains("@Nullable");
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
