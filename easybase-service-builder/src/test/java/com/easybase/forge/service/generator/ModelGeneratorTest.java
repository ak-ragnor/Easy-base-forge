package com.easybase.forge.service.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.service.config.ServiceConfig;
import com.easybase.forge.service.config.ServiceConfigLoader;

/**
 * Tests for {@link ModelGenerator}: verifies the developer-owned model shell.
 *
 * <p>The shell extends {@code *Base} and contains no field declarations — all
 * generated fields live in {@code *Base}. Developers add custom fields here.
 */
class ModelGeneratorTest {

	@TempDir
	Path outputDir;

	@Test
	void generate_producesClassDeclaration() {
		String source = generateSource();

		assertThat(source).contains("public class User");
	}

	@Test
	void generate_extendsUserBase() {
		String source = generateSource();

		assertThat(source).contains("extends UserBase");
	}

	@Test
	void generate_hasNoFieldDeclarations() {
		String source = generateSource();

		assertThat(source).doesNotContain("UUID id");
		assertThat(source).doesNotContain("String email");
		assertThat(source).doesNotContain("String firstName");
		assertThat(source).doesNotContain("Instant createdAt");
		assertThat(source).doesNotContain("Boolean deleted");
	}

	@Test
	void generate_hasNoLombokAnnotations() {
		String source = generateSource();

		assertThat(source).doesNotContain("@Getter");
		assertThat(source).doesNotContain("@Setter");
	}

	@Test
	void generate_outputPathEndsWithModelUserJava() {
		List<GeneratedArtifact> artifacts = generate();

		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).outputPath().toString()).endsWith("model/User.java");
	}

	@Test
	void generate_packageDeclarationMatchesModelPackage() {
		String source = generateSource();

		assertThat(source).contains("package com.example.app.user.domain.model;");
	}

	private String generateSource() {
		List<GeneratedArtifact> artifacts = generate();
		assertThat(artifacts).hasSize(1);
		return artifacts.get(0).content();
	}

	private List<GeneratedArtifact> generate() {
		ServiceConfig config = loadConfig();
		return new ModelGenerator().generate(config);
	}

	private ServiceConfig loadConfig() {
		URL url = getClass().getResource("/service/" + "user.yml");
		assertThat(url).as("Fixture not found: " + "user.yml").isNotNull();
		return ServiceConfigLoader.load(Paths.get(url.getPath()), outputDir);
	}
}
