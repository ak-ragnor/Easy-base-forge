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
 * Tests for {@link BaseEntityGenerator}: verifies the @MappedSuperclass
 * output under different audit configurations.
 */
class BaseEntityGeneratorTest {

	@TempDir
	Path outputDir;

	// -------------------------------------------------------------------------
	// Class-level declarations
	// -------------------------------------------------------------------------

	@Test
	void generate_hasMappedSuperclassAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@MappedSuperclass");
	}

	@Test
	void generate_producesAbstractClass() {
		String source = generateSource("user.yml");

		assertThat(source).contains("abstract class BaseEntity");
	}

	@Test
	void generate_hasGetterSetterAnnotations() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@Getter");
		assertThat(source).contains("@Setter");
	}

	// -------------------------------------------------------------------------
	// Audit timestamp fields
	// -------------------------------------------------------------------------

	@Test
	void generate_hasCreationTimestampAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@CreationTimestamp");
	}

	@Test
	void generate_hasUpdateTimestampAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@UpdateTimestamp");
	}

	@Test
	void generate_hasCreatedAtAndUpdatedAtFields() {
		String source = generateSource("user.yml");

		assertThat(source).contains("createdAt");
		assertThat(source).contains("updatedAt");
	}

	// -------------------------------------------------------------------------
	// Auditor type variants
	// -------------------------------------------------------------------------

	@Test
	void generate_uuidAuditorType_generatesUuidCreatedByAndUpdatedBy() {
		// user.yml has auditorType: UUID
		String source = generateSource("user.yml");

		assertThat(source).contains("UUID createdBy");
		assertThat(source).contains("UUID updatedBy");
	}

	@Test
	void generate_stringAuditorType_generatesStringCreatedByAndUpdatedBy() {
		// product.yml has auditorType: String
		String source = generateSource("product.yml");

		assertThat(source).contains("String createdBy");
		assertThat(source).contains("String updatedBy");
	}

	// -------------------------------------------------------------------------
	// Soft delete field
	// -------------------------------------------------------------------------

	@Test
	void generate_includesSoftDeleteField_whenEnabled() {
		// user.yml has softDelete: true, softDeleteColumn: deleted
		String source = generateSource("user.yml");

		assertThat(source).contains("Boolean deleted");
	}

	@Test
	void generate_excludesSoftDeleteField_whenDisabled() {
		// product.yml has softDelete: false
		String source = generateSource("product.yml");

		assertThat(source).doesNotContain("Boolean deleted");
		assertThat(source).doesNotContain("private Boolean");
	}

	// -------------------------------------------------------------------------
	// Audit disabled — generator returns empty list
	// -------------------------------------------------------------------------

	@Test
	void generate_returnsEmptyList_whenAuditDisabled() {
		// post.yml has audit.enabled: false — no BaseEntity should be generated
		List<GeneratedArtifact> artifacts = generate("post.yml");

		assertThat(artifacts).isEmpty();
	}

	// -------------------------------------------------------------------------
	// Output path
	// -------------------------------------------------------------------------

	@Test
	void generate_outputPathIsInPersistencePackage() {
		List<GeneratedArtifact> artifacts = generate("user.yml");

		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).outputPath().toString()).endsWith("persistence/BaseEntity.java");
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
		return new BaseEntityGenerator().generate(config);
	}

	private ServiceConfig loadConfig(String yamlResource) {
		URL url = getClass().getResource("/service/" + yamlResource);
		assertThat(url).as("Fixture not found: " + yamlResource).isNotNull();
		return ServiceConfigLoader.load(Paths.get(url.getPath()), outputDir);
	}
}
