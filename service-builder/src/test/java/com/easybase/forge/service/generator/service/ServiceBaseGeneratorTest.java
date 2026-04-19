package com.easybase.forge.service.generator.service;

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

class ServiceBaseGeneratorTest {

	@TempDir
	Path outputDir;

	@Test
	void generate_isInterface() {
		String source = generateSource("user.yml");

		assertThat(source).contains("interface UserServiceBase");
	}

	@Test
	void generate_extendsRuntimeBaseService() {
		String source = generateSource("user.yml");

		assertThat(source).contains("BaseService");
	}

	@Test
	void generate_typeParametersIncludeModelAndId() {
		String source = generateSource("user.yml");

		assertThat(source).contains("BaseService<User, UUID>");
	}

	@Test
	void generate_typeParametersUseLongForLongIdType() {
		String source = generateSource("product.yml");

		assertThat(source).contains("BaseService<Product, Long>");
	}

	@Test
	void generate_noDisabledWarnings_whenAllCrudEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).doesNotContain("Disabled");
	}

	@Test
	void generate_javadocWarning_whenDeleteDisabled() {
		String source = generateSource("product.yml");

		assertThat(source).contains("Disabled");
		assertThat(source).contains("delete");
	}

	@Test
	void generate_outputPathIsInServiceBasePackage() {
		List<GeneratedArtifact> artifacts = generate("user.yml");

		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).outputPath().toString()).endsWith("user/service/base/UserServiceBase.java");
	}

	private String generateSource(String yamlResource) {
		List<GeneratedArtifact> artifacts = generate(yamlResource);
		assertThat(artifacts).hasSize(1);
		return artifacts.get(0).content();
	}

	private List<GeneratedArtifact> generate(String yamlResource) {
		ServiceConfig config = loadConfig(yamlResource);
		return new ServiceBaseGenerator().generate(config);
	}

	private ServiceConfig loadConfig(String yamlResource) {
		URL url = getClass().getResource("/service/" + yamlResource);
		assertThat(url).as("Fixture not found: " + yamlResource).isNotNull();
		return ServiceConfigLoader.load(Paths.get(url.getPath()), outputDir);
	}
}
