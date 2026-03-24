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
 * Verifies the addGeneratedAnnotation config: @generated/@author Javadoc on auto-overwritten
 * files and cURL snippets on base controller methods.
 */
class GeneratedAnnotationTest {

	@TempDir
	Path outputDir;

	private GeneratorEngine engine(boolean addAnnotation, String author) {
		GeneratorConfig config = new GeneratorConfig();
		config.setBasePackage("com.example.api");

		GenerateOptions opts = new GenerateOptions();
		opts.setAddGeneratedAnnotation(addAnnotation);
		if (author != null) opts.setAuthor(author);
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
	void annotationEnabled_baseControllerHasGeneratedTag() throws Exception {
		engine(true, null).generate(specPath());

		String content =
				Files.readString(outputDir.resolve("com/example/api/pets/controller/base/PetsControllerBase.java"));

		assertThat(content).contains("@generated");
	}

	@Test
	void annotationEnabled_withAuthor_includesAuthorTag() throws Exception {
		engine(true, "Jane Doe").generate(specPath());

		String content =
				Files.readString(outputDir.resolve("com/example/api/pets/controller/base/PetsControllerBase.java"));

		assertThat(content).contains("@author Jane Doe");
		assertThat(content).contains("@generated");
	}

	@Test
	void annotationEnabled_methodHasCurlSnippet() throws Exception {
		engine(true, null).generate(specPath());

		String content =
				Files.readString(outputDir.resolve("com/example/api/pets/controller/base/PetsControllerBase.java"));

		assertThat(content).contains("curl -X GET");
		assertThat(content).contains("http://localhost:8080");
		assertThat(content).contains("/pets");
	}

	@Test
	void annotationEnabled_postMethodHasContentTypeHeader() throws Exception {
		engine(true, null).generate(specPath());

		String content =
				Files.readString(outputDir.resolve("com/example/api/pets/controller/base/PetsControllerBase.java"));

		assertThat(content).contains("curl -X POST");
		assertThat(content).contains("Content-Type: application/json");
	}

	@Test
	void annotationEnabled_delegateHasGeneratedTag() throws Exception {
		engine(true, null).generate(specPath());

		String content = Files.readString(outputDir.resolve("com/example/api/pets/delegate/PetsApiDelegate.java"));

		assertThat(content).contains("@generated");
	}

	@Test
	void annotationEnabled_dtoHasGeneratedTag() throws Exception {
		engine(true, null).generate(specPath());

		String content = Files.readString(outputDir.resolve("com/example/api/pets/dto/PetDTO.java"));

		assertThat(content).contains("@generated");
	}

	@Test
	void annotationDisabled_noJavadocAdded() throws Exception {
		engine(false, null).generate(specPath());

		String baseCtrl =
				Files.readString(outputDir.resolve("com/example/api/pets/controller/base/PetsControllerBase.java"));
		assertThat(baseCtrl).doesNotContain("@generated");
		assertThat(baseCtrl).doesNotContain("curl -X");

		String delegate = Files.readString(outputDir.resolve("com/example/api/pets/delegate/PetsApiDelegate.java"));

		assertThat(delegate).doesNotContain("@generated");
	}
}
