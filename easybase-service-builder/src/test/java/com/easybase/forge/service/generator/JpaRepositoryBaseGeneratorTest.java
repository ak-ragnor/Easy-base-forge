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

class JpaRepositoryBaseGeneratorTest {

	@TempDir
	Path outputDir;

	@Test
	void generate_hasNoRepositoryBeanAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@NoRepositoryBean");
	}

	@Test
	void generate_isInterface() {
		String source = generateSource("user.yml");

		assertThat(source).contains("interface UserJpaRepositoryBase");
	}

	@Test
	void generate_extendsJpaRepository() {
		String source = generateSource("user.yml");

		assertThat(source).contains("JpaRepository");
		assertThat(source).contains("UserEntity");
		assertThat(source).contains("UUID");
	}

	@Test
	void generate_includesFindActiveById_whenSoftDeleteEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("findActiveById(");
	}

	@Test
	void generate_includesFindAllActive_whenSoftDeleteEnabled() {
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
		String source = generateSource("product.yml");

		assertThat(source).doesNotContain("findActiveById");
		assertThat(source).doesNotContain("findAllActive");
	}

	@Test
	void generate_usesLongIdType_whenConfigured() {
		String source = generateSource("product.yml");

		assertThat(source).contains("JpaRepository");
		assertThat(source).contains("ProductEntity");
		assertThat(source).contains("Long");
	}

	@Test
	void generate_outputPathIsInPersistenceBasePackage() {
		List<GeneratedArtifact> artifacts = generate("user.yml");

		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).outputPath().toString())
				.endsWith("user/infrastructure/persistence/base/UserJpaRepositoryBase.java");
	}

	private String generateSource(String yamlResource) {
		List<GeneratedArtifact> artifacts = generate(yamlResource);
		assertThat(artifacts).hasSize(1);
		return artifacts.get(0).content();
	}

	private List<GeneratedArtifact> generate(String yamlResource) {
		ServiceConfig config = loadConfig(yamlResource);
		return new JpaRepositoryBaseGenerator().generate(config);
	}

	private ServiceConfig loadConfig(String yamlResource) {
		URL url = getClass().getResource("/service/" + yamlResource);
		assertThat(url).as("Fixture not found: " + yamlResource).isNotNull();
		return ServiceConfigLoader.load(Paths.get(url.getPath()), outputDir);
	}
}
