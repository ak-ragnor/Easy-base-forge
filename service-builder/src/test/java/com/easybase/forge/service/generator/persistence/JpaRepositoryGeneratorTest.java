package com.easybase.forge.service.generator.persistence;

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
 * Tests for {@link JpaRepositoryGenerator}: verifies the developer-owned stub
 * Spring Data JPA repository interface extending the generated base.
 */
class JpaRepositoryGeneratorTest {

	@TempDir
	Path outputDir;

	@Test
	void generate_isInterface() {
		String source = generateSource("user.yml");

		assertThat(source).contains("interface UserJpaRepository");
	}

	@Test
	void generate_extendsJpaRepositoryBase() {
		String source = generateSource("user.yml");

		assertThat(source).contains("extends UserJpaRepositoryBase");
	}

	@Test
	void generate_hasNoSoftDeleteMethods() {
		String source = generateSource("user.yml");

		assertThat(source).doesNotContain("findActiveById");
		assertThat(source).doesNotContain("findAllActive");
	}

	@Test
	void generate_hasNoRepositoryBeanAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).doesNotContain("@NoRepositoryBean");
	}

	@Test
	void generate_outputPathIsInPersistencePackage() {
		List<GeneratedArtifact> artifacts = generate("user.yml");

		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).outputPath().toString())
				.endsWith("user/infrastructure/persistence/UserJpaRepository.java");
	}

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
