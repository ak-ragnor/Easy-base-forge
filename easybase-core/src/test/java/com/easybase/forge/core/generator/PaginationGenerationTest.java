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
import com.easybase.forge.core.writer.GenerationReport;

/**
 * Verifies that PaginationMode.SPRING_DATA injects Pageable and uses Page<T> return types.
 */
class PaginationGenerationTest {

	@TempDir
	Path outputDir;

	private GeneratorEngine engine(PaginationMode paginationMode) {
		GeneratorConfig config = new GeneratorConfig();
		config.setBasePackage("com.example.api");

		GenerateOptions opts = new GenerateOptions();
		opts.setResponseEntityWrapping(ResponseEntityMode.ALWAYS);
		opts.setBeanValidation(true);
		opts.setPagination(paginationMode);
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

	@Test
	void springDataPagination_delegateContainsPageableAndPageReturn() throws Exception {
		GenerationReport report = engine(PaginationMode.SPRING_DATA).generate(specPath());
		assertThat(report.errors()).isEmpty();

		Path delegate = outputDir.resolve("com/example/api/pets/delegate/PetsApiDelegate.java");
		assertThat(delegate).exists();

		String content = Files.readString(delegate);

		assertThat(content).contains("Pageable pageable");
		assertThat(content).contains("Page<");
		assertThat(content).contains("import org.springframework.data.domain.Page");
		assertThat(content).contains("import org.springframework.data.domain.Pageable");
	}

	@Test
	void springDataPagination_baseControllerContainsPageableParam() throws Exception {
		GenerationReport report = engine(PaginationMode.SPRING_DATA).generate(specPath());
		assertThat(report.errors()).isEmpty();

		Path baseCtrl = outputDir.resolve("com/example/api/pets/controller/base/PetsControllerBase.java");

		assertThat(baseCtrl).exists();

		String content = Files.readString(baseCtrl);
		assertThat(content).contains("Pageable pageable");
		assertThat(content).contains("Page<");
	}

	@Test
	void nonPaginatedMode_noPageableInjected() throws Exception {
		GenerationReport report = engine(PaginationMode.NONE).generate(specPath());
		assertThat(report.errors()).isEmpty();

		Path delegate = outputDir.resolve("com/example/api/pets/delegate/PetsApiDelegate.java");

		assertThat(delegate).exists();

		String content = Files.readString(delegate);
		assertThat(content).doesNotContain("Pageable");
		assertThat(content).doesNotContain("Page<");
	}
}
