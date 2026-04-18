package com.easybase.forge.core.service.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.easybase.forge.core.service.config.ServiceConfigLoader;

class ModelBaseGeneratorTest {

	@TempDir
	Path outputDir;

	@Test
	void generate_producesClassDeclaration() {
		String source = generateSource("user.yml");

		assertThat(source).contains("public class UserBase");
	}

	@Test
	void generate_doesNotExtendAnyClass() {
		String source = generateSource("user.yml");

		assertThat(source).doesNotContain("extends ");
	}

	@Test
	void generate_hasLombokAnnotations() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@Getter");
		assertThat(source).contains("@Setter");
		assertThat(source).contains("@NoArgsConstructor");
		assertThat(source).contains("@AllArgsConstructor");
	}

	@Test
	void generate_includesIdField() {
		String source = generateSource("user.yml");

		assertThat(source).contains("UUID id");
	}

	@Test
	void generate_includesAllDeclaredFields() {
		String source = generateSource("user.yml");

		assertThat(source).contains("String email");
		assertThat(source).contains("String firstName");
	}

	@Test
	void generate_includesOneToOneFkIdField() {
		String source = generateSource("user.yml");

		assertThat(source).contains("UUID tenantId");
	}

	@Test
	void generate_excludesOneToManyFromModel() {
		String source = generateSource("user.yml");

		assertThat(source).doesNotContain("List");
	}

	@Test
	void generate_includesAuditFieldsWhenEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("Instant createdAt");
		assertThat(source).contains("UUID createdBy");
		assertThat(source).contains("Instant updatedAt");
		assertThat(source).contains("UUID updatedBy");
	}

	@Test
	void generate_excludesAuditFields_whenDisabled() {
		String source = generateSource("post.yml");

		assertThat(source).doesNotContain("createdAt");
		assertThat(source).doesNotContain("createdBy");
	}

	@Test
	void generate_includesSoftDeleteField_whenEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("Boolean deleted");
	}

	@Test
	void generate_excludesSoftDeleteField_whenDisabled() {
		String source = generateSource("post.yml");

		assertThat(source).doesNotContain("deleted");
	}

	@Test
	void generate_importsUuid() {
		String source = generateSource("user.yml");

		assertThat(source).contains("import java.util.UUID;");
	}

	@Test
	void generate_importsInstant() {
		String source = generateSource("user.yml");

		assertThat(source).contains("import java.time.Instant;");
	}

	@Test
	void generate_outputPathEndsWithModelUserBaseJava() {
		List<GeneratedArtifact> artifacts = generate("user.yml");

		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).outputPath().toString()).endsWith("model/UserBase.java");
	}

	@Test
	void generate_packageDeclarationMatchesModelPackage() {
		String source = generateSource("user.yml");

		assertThat(source).contains("package com.example.app.user.domain.model;");
	}

	private String generateSource(String yamlResource) {
		List<GeneratedArtifact> artifacts = generate(yamlResource);
		assertThat(artifacts).hasSize(1);
		return artifacts.get(0).content();
	}

	private List<GeneratedArtifact> generate(String yamlResource) {
		ServiceConfig config = loadConfig(yamlResource);
		return new ModelBaseGenerator().generate(config);
	}

	private ServiceConfig loadConfig(String yamlResource) {
		URL url = getClass().getResource("/service/" + yamlResource);
		assertThat(url).as("Fixture not found: " + yamlResource).isNotNull();
		return ServiceConfigLoader.load(Paths.get(url.getPath()), outputDir);
	}
}
