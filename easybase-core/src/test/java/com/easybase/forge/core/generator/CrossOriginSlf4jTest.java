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
 * Verifies that {@code generate.crossOrigin} and {@code generate.slf4j} config
 * options add the corresponding annotations to the generated custom controller
 * scaffold (the user-owned, never-overwritten file).
 */
class CrossOriginSlf4jTest {

	@TempDir
	Path outputDir;

	private GeneratorEngine engine(String crossOrigin, boolean slf4j) throws Exception {
		GeneratorConfig config = new GeneratorConfig();
		config.setBasePackage("com.example.api");

		GenerateOptions opts = new GenerateOptions();
		opts.setCrossOrigin(crossOrigin);
		opts.setSlf4j(slf4j);
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
	void crossOrigin_wildcard_addsAnnotationToCustomController() throws Exception {
		engine("*", false).generate(specPath());

		String ctrl = Files.readString(outputDir.resolve("com/example/api/pets/controller/PetsController.java"));

		assertThat(ctrl).contains("@CrossOrigin");
		assertThat(ctrl).contains("origins = \"*\"");
	}

	@Test
	void crossOrigin_specificDomain_addsAnnotation() throws Exception {
		engine("https://example.com", false).generate(specPath());

		String ctrl = Files.readString(outputDir.resolve("com/example/api/pets/controller/PetsController.java"));

		assertThat(ctrl).contains("@CrossOrigin");
		assertThat(ctrl).contains("origins = \"https://example.com\"");
	}

	@Test
	void slf4j_true_addsSlf4jAnnotation() throws Exception {
		engine(null, true).generate(specPath());

		String ctrl = Files.readString(outputDir.resolve("com/example/api/pets/controller/PetsController.java"));

		assertThat(ctrl).contains("@Slf4j");
	}

	@Test
	void crossOriginAndSlf4j_both_addBothAnnotations() throws Exception {
		engine("*", true).generate(specPath());

		String ctrl = Files.readString(outputDir.resolve("com/example/api/pets/controller/PetsController.java"));

		assertThat(ctrl).contains("@CrossOrigin");
		assertThat(ctrl).contains("@Slf4j");
	}

	@Test
	void noCrossOrigin_noAnnotation() throws Exception {
		engine(null, false).generate(specPath());

		String ctrl = Files.readString(outputDir.resolve("com/example/api/pets/controller/PetsController.java"));

		assertThat(ctrl).doesNotContain("@CrossOrigin");
	}

	@Test
	void slf4j_false_noAnnotation() throws Exception {
		engine("*", false).generate(specPath());

		String ctrl = Files.readString(outputDir.resolve("com/example/api/pets/controller/PetsController.java"));

		assertThat(ctrl).doesNotContain("@Slf4j");
	}

	@Test
	void baseController_neverGets_crossOriginOrSlf4j() throws Exception {
		engine("*", true).generate(specPath());

		String base =
				Files.readString(outputDir.resolve("com/example/api/pets/controller/base/PetsControllerBase.java"));

		assertThat(base).doesNotContain("@CrossOrigin");
		assertThat(base).doesNotContain("@Slf4j");
	}
}
