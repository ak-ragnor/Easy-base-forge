package com.easybase.forge.core.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.easybase.forge.core.config.*;
import com.easybase.forge.core.engine.GeneratorEngine;

/**
 * Verifies that multiple {@code @author} Javadoc tags are emitted when
 * {@code generate.authors} is configured, and that the legacy single
 * {@code generate.author} still works.
 */
class MultipleAuthorsTest {

	@TempDir
	Path outputDir;

	private GeneratorEngine engine(String singleAuthor, List<String> authors) throws Exception {
		GeneratorConfig config = new GeneratorConfig();
		config.setBasePackage("com.example.api");

		GenerateOptions opts = new GenerateOptions();
		opts.setAddGeneratedAnnotation(true);
		if (singleAuthor != null) opts.setAuthor(singleAuthor);
		if (authors != null) opts.setAuthors(authors);
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
	void authorsList_generatesMultipleAuthorTags() throws Exception {
		engine(null, List.of("Alice", "Bob")).generate(specPath());

		String base =
				Files.readString(outputDir.resolve("com/example/api/pets/controller/base/PetsControllerBase.java"));

		assertThat(base).contains("@author Alice");
		assertThat(base).contains("@author Bob");
		assertThat(base).contains("@generated");
	}

	@Test
	void authorsList_appliedToDelegate() throws Exception {
		engine(null, List.of("Alice", "Bob")).generate(specPath());

		String delegate = Files.readString(outputDir.resolve("com/example/api/pets/delegate/PetsApiDelegate.java"));

		assertThat(delegate).contains("@author Alice");
		assertThat(delegate).contains("@author Bob");
	}

	@Test
	void authorsList_appliedToDto() throws Exception {
		engine(null, List.of("Alice", "Bob")).generate(specPath());

		String dto = Files.readString(outputDir.resolve("com/example/api/pets/dto/PetDTO.java"));

		assertThat(dto).contains("@author Alice");
		assertThat(dto).contains("@author Bob");
	}

	@Test
	void legacyAuthorField_stillWorks() throws Exception {
		engine("Jane Doe", null).generate(specPath());

		String base =
				Files.readString(outputDir.resolve("com/example/api/pets/controller/base/PetsControllerBase.java"));

		assertThat(base).contains("@author Jane Doe");
		assertThat(base).contains("@generated");
	}

	@Test
	void authorAndAuthors_bothAppear_authorFirst() throws Exception {
		engine("Primary", List.of("Secondary", "Tertiary")).generate(specPath());

		String base =
				Files.readString(outputDir.resolve("com/example/api/pets/controller/base/PetsControllerBase.java"));

		assertThat(base).contains("@author Primary");
		assertThat(base).contains("@author Secondary");
		assertThat(base).contains("@author Tertiary");

		int primaryIdx = base.indexOf("@author Primary");
		int secondaryIdx = base.indexOf("@author Secondary");
		assertThat(primaryIdx).isLessThan(secondaryIdx);
	}

	@Test
	void noDuplicateAuthors_whenAuthorAlsoInList() throws Exception {
		engine("Alice", List.of("Alice", "Bob")).generate(specPath());

		String base =
				Files.readString(outputDir.resolve("com/example/api/pets/controller/base/PetsControllerBase.java"));

		long count = base.lines().filter(l -> l.contains("@author Alice")).count();
		assertThat(count).isEqualTo(1);
	}
}
