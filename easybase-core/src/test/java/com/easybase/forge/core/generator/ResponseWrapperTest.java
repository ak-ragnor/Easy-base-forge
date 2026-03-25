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
 * Verifies that {@code generate.responseWrapper.enabled: true} causes the
 * generator to emit custom wrapper types ({@code ApiResponse<T>} /
 * {@code ApiPageResponse<T>}) instead of {@code ResponseEntity<T>},
 * and that {@code @ResponseStatus} is inferred from the HTTP status code.
 */
class ResponseWrapperTest {

	private static final String SINGLE_CLASS = "com.example.wrapper.ApiResponse";
	private static final String PAGED_CLASS = "com.example.wrapper.ApiPageResponse";

	@TempDir
	Path outputDir;

	private GeneratorEngine engine() throws Exception {
		GeneratorConfig config = new GeneratorConfig();
		config.setBasePackage("com.example.api");

		GenerateOptions opts = new GenerateOptions();
		opts.setResponseEntityWrapping(ResponseEntityMode.NEVER);
		opts.setPagination(PaginationMode.SPRING_DATA);

		ResponseWrapperConfig wrapper = new ResponseWrapperConfig();
		wrapper.setEnabled(true);
		wrapper.setSingleClass(SINGLE_CLASS);
		wrapper.setPagedClass(PAGED_CLASS);
		opts.setResponseWrapper(wrapper);
		config.setGenerate(opts);

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
	void delegate_usesCustomSingleWrapper() throws Exception {
		engine().generate(specPath());

		String delegate = Files.readString(outputDir.resolve("com/example/api/delegate/UsersApiDelegate.java"));

		assertThat(delegate).contains("ApiResponse<UserDto>");
		assertThat(delegate).doesNotContain("ResponseEntity");
	}

	@Test
	void delegate_usesCustomPagedWrapper_forPaginatedEndpoint() throws Exception {
		engine().generate(specPath());

		String delegate = Files.readString(outputDir.resolve("com/example/api/delegate/UsersApiDelegate.java"));

		assertThat(delegate).contains("ApiPageResponse<UserDto>");
	}

	@Test
	void baseController_usesCustomWrapper() throws Exception {
		engine().generate(specPath());

		String base = Files.readString(outputDir.resolve("com/example/api/controller/base/UsersControllerBase.java"));

		assertThat(base).contains("ApiResponse<UserDto>");
		assertThat(base).doesNotContain("ResponseEntity");
	}

	@Test
	void baseController_hasResponseStatusCreated_for201() throws Exception {
		engine().generate(specPath());

		String base = Files.readString(outputDir.resolve("com/example/api/controller/base/UsersControllerBase.java"));

		assertThat(base).contains("@ResponseStatus");
		assertThat(base).contains("HttpStatus.CREATED");
	}

	@Test
	void baseController_hasResponseStatusNoContent_for204() throws Exception {
		engine().generate(specPath());

		String base = Files.readString(outputDir.resolve("com/example/api/controller/base/UsersControllerBase.java"));

		assertThat(base).contains("HttpStatus.NO_CONTENT");
	}

	@Test
	void baseController_voidReturn_for204Endpoint() throws Exception {
		engine().generate(specPath());

		String base = Files.readString(outputDir.resolve("com/example/api/controller/base/UsersControllerBase.java"));

		assertThat(base).contains("void deleteUser(");
	}

	@Test
	void existingResponseEntityMode_unaffected_whenWrapperDisabled() throws Exception {
		GeneratorConfig config = new GeneratorConfig();
		config.setBasePackage("com.example.api");

		OutputConfig output = new OutputConfig();
		output.setLayout(LayoutMode.FLAT);
		config.setOutput(output);
		config.withOutputDirectory(outputDir);

		new GeneratorEngine(config).generate(specPath());

		String delegate = Files.readString(outputDir.resolve("com/example/api/delegate/UsersApiDelegate.java"));
		assertThat(delegate).contains("ResponseEntity");
		assertThat(delegate).doesNotContain("ApiResponse");
	}
}
