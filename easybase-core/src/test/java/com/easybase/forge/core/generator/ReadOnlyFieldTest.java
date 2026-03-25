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
 * Verifies that OpenAPI {@code readOnly: true} properties are emitted with
 * {@code @JsonProperty(value = "...", access = JsonProperty.Access.READ_ONLY)}
 * in the generated DTO, while non-readOnly fields keep the plain form.
 */
class ReadOnlyFieldTest {

	@TempDir
	Path outputDir;

	private GeneratorEngine engine() throws Exception {
		GeneratorConfig config = new GeneratorConfig();
		config.setBasePackage("com.example.api");

		OutputConfig output = new OutputConfig();
		output.setLayout(LayoutMode.FLAT);
		config.setOutput(output);
		config.withOutputDirectory(outputDir);

		return new GeneratorEngine(config);
	}

	private Path specPath() throws Exception {
		URL url = getClass().getResource("/specs/userapi.yaml");
		assertThat(url).as("userapi.yaml not found").isNotNull();
		return Paths.get(url.toURI());
	}

	@Test
	void readOnlyField_hasAccessReadOnly() throws Exception {
		engine().generate(specPath());

		String dto = Files.readString(outputDir.resolve("com/example/api/dto/UserDto.java"));

		assertThat(dto).contains("access = JsonProperty.Access.READ_ONLY");
	}

	@Test
	void nonReadOnlyField_hasNoAccessMember() throws Exception {
		engine().generate(specPath());

		String dto = Files.readString(outputDir.resolve("com/example/api/dto/UserDto.java"));

		assertThat(dto).contains("@JsonProperty(\"email\")");
	}

	@Test
	void readOnlyField_preservesJsonName() throws Exception {
		engine().generate(specPath());

		String dto = Files.readString(outputDir.resolve("com/example/api/dto/UserDto.java"));

		assertThat(dto).contains("value = \"id\"");
		assertThat(dto).contains("value = \"createdAt\"");
		assertThat(dto).satisfies(content -> {
			assertThat(content.indexOf("value = \"id\"")).isLessThan(content.indexOf("email"));
		});
	}
}
