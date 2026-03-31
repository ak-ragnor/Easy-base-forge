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
 * Tests for {@link JpaRepositoryGenerator}: verifies Spring Data JPA repository
 * interface generation including soft-delete query methods.
 */
class JpaRepositoryGeneratorTest {

	@TempDir
	Path outputDir;

	// -------------------------------------------------------------------------
	// Interface declaration
	// -------------------------------------------------------------------------

	@Test
	void generate_hasNoRepositoryBeanAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@NoRepositoryBean");
	}

	@Test
	void generate_extendsJpaRepository() {
		String source = generateSource("user.yml");

		assertThat(source).contains("JpaRepository");
		assertThat(source).contains("UserEntity");
		assertThat(source).contains("UUID");
	}

	@Test
	void generate_isInterface() {
		String source = generateSource("user.yml");

		assertThat(source).contains("interface UserJpaRepository");
	}

	// -------------------------------------------------------------------------
	// Soft-delete query methods
	// -------------------------------------------------------------------------

	@Test
	void generate_includesFindActiveById_whenSoftDeleteEnabled() {
		// user.yml has softDelete: true
		String source = generateSource("user.yml");

		assertThat(source).contains("findActiveById(");
	}

	@Test
	void generate_includesFindAllActive_whenSoftDeleteEnabled() {
		// user.yml has softDelete: true
		String source = generateSource("user.yml");

		assertThat(source).contains("findAllActive(");
	}

	@Test
	void generate_softDeleteQuery_filtersOnDeletedFalse() {
		String source = generateSource("user.yml");

		assertThat(source).contains("deleted = false");
	}

	@Test
	void generate_softDeleteQuery_hasQueryAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@Query");
	}

	@Test
	void generate_excludesSoftDeleteQueries_whenSoftDeleteDisabled() {
		// product.yml has softDelete: false
		String source = generateSource("product.yml");

		assertThat(source).doesNotContain("findActiveById");
		assertThat(source).doesNotContain("findAllActive");
	}

	// -------------------------------------------------------------------------
	// Long ID type
	// -------------------------------------------------------------------------

	@Test
	void generate_usesLongIdType_whenConfigured() {
		// product.yml has idType: Long
		String source = generateSource("product.yml");

		assertThat(source).contains("JpaRepository");
		assertThat(source).contains("ProductEntity");
		assertThat(source).contains("Long");
	}

	// -------------------------------------------------------------------------
	// Output path
	// -------------------------------------------------------------------------

	@Test
	void generate_outputPathIsInPersistencePackage() {
		List<GeneratedArtifact> artifacts = generate("user.yml");

		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).outputPath().toString()).endsWith("persistence/UserJpaRepository.java");
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
		return new JpaRepositoryGenerator().generate(config);
	}

	private ServiceConfig loadConfig(String yamlResource) {
		URL url = getClass().getResource("/service/" + yamlResource);
		assertThat(url).as("Fixture not found: " + yamlResource).isNotNull();
		return ServiceConfigLoader.load(Paths.get(url.getPath()), outputDir);
	}
}
