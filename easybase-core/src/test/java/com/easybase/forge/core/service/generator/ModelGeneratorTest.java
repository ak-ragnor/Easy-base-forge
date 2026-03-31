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

/**
 * Tests for {@link ModelGenerator}: verifies the generated Java record source
 * for correctness across various entity configurations.
 */
class ModelGeneratorTest {

	@TempDir
	Path outputDir;

	// -------------------------------------------------------------------------
	// Record structure
	// -------------------------------------------------------------------------

	@Test
	void generate_producesRecordDeclaration() {
		String source = generateSource("user.yml");

		assertThat(source).contains("public record User(");
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
	void generate_recordBodyIsEmpty() {
		String source = generateSource("user.yml");

		// Records in this generator have no body methods
		assertThat(source).contains(") {}");
	}

	// -------------------------------------------------------------------------
	// Relationships
	// -------------------------------------------------------------------------

	@Test
	void generate_includesManyToOneFkField() {
		String source = generateSource("user.yml");

		// Tenant MANY_TO_ONE with column tenant_id → UUID tenantId
		assertThat(source).contains("UUID tenantId");
	}

	@Test
	void generate_excludesOneToManyRelationship() {
		String source = generateSource("user.yml");

		// ONE_TO_MANY (posts) should NOT appear in the record — it's the non-owning side
		assertThat(source).doesNotContain("posts");
		assertThat(source).doesNotContain("List");
	}

	// -------------------------------------------------------------------------
	// Audit fields
	// -------------------------------------------------------------------------

	@Test
	void generate_includesAuditFieldsWhenEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("Instant createdAt");
		assertThat(source).contains("UUID createdBy");
		assertThat(source).contains("Instant updatedAt");
		assertThat(source).contains("UUID updatedBy");
	}

	@Test
	void generate_includesSoftDeleteField_whenEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("Boolean deleted");
	}

	@Test
	void generate_excludesAuditFields_whenDisabled() {
		String source = generateSource("post.yml");

		assertThat(source).doesNotContain("createdAt");
		assertThat(source).doesNotContain("createdBy");
		assertThat(source).doesNotContain("updatedAt");
		assertThat(source).doesNotContain("updatedBy");
	}

	@Test
	void generate_excludesSoftDeleteField_whenAuditDisabled() {
		String source = generateSource("post.yml");

		assertThat(source).doesNotContain("deleted");
	}

	@Test
	void generate_stringAuditorType_inRecord() {
		String source = generateSource("product.yml");

		// product.yml has auditorType: String
		assertThat(source).contains("String createdBy");
		assertThat(source).contains("String updatedBy");
	}

	// -------------------------------------------------------------------------
	// Imports
	// -------------------------------------------------------------------------

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
	void generate_noImportsForStringAuditorType() {
		String source = generateSource("product.yml");

		// String is in java.lang — no import needed
		assertThat(source).doesNotContain("import java.lang.String");
	}

	// -------------------------------------------------------------------------
	// Output path
	// -------------------------------------------------------------------------

	@Test
	void generate_outputPathEndsWithModelUserJava() {
		List<GeneratedArtifact> artifacts = generate("user.yml");

		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).outputPath().toString()).endsWith("model/User.java");
	}

	@Test
	void generate_packageDeclarationMatchesModelPackage() {
		String source = generateSource("user.yml");

		assertThat(source).contains("package com.example.app.model;");
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private String generateSource(String yamlResource) {
		List<GeneratedArtifact> artifacts = generate(yamlResource);
		assertThat(artifacts).hasSize(1);
		return artifacts.get(0).content();
	}

	private List<GeneratedArtifact> generate(String yamlResource) {
		ServiceConfig config = loadConfig(yamlResource);
		return new ModelGenerator().generate(config);
	}

	private ServiceConfig loadConfig(String yamlResource) {
		URL url = getClass().getResource("/service/" + yamlResource);
		assertThat(url).as("Fixture not found: " + yamlResource).isNotNull();
		return ServiceConfigLoader.load(Paths.get(url.getPath()), outputDir);
	}
}
