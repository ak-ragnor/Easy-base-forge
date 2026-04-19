package com.easybase.forge.service.generator.hook;

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

class HookBaseGeneratorTest {

	@TempDir
	Path outputDir;

	@Test
	void generate_isInterface() {
		String source = generateSource("user.yml");

		assertThat(source).contains("interface UserHookBase");
	}

	@Test
	void generate_hasBeforeSaveMethod() {
		String source = generateSource("user.yml");

		assertThat(source).contains("beforeSave(");
	}

	@Test
	void generate_hasAfterSaveMethod() {
		String source = generateSource("user.yml");

		assertThat(source).contains("afterSave(");
	}

	@Test
	void generate_hasBeforeUpdateMethod() {
		String source = generateSource("user.yml");

		assertThat(source).contains("beforeUpdate(");
	}

	@Test
	void generate_hasAfterUpdateMethod() {
		String source = generateSource("user.yml");

		assertThat(source).contains("afterUpdate(");
	}

	@Test
	void generate_hasBeforeDeleteMethod() {
		String source = generateSource("user.yml");

		assertThat(source).contains("beforeDelete(");
	}

	@Test
	void generate_hasAfterDeleteMethod() {
		String source = generateSource("user.yml");

		assertThat(source).contains("afterDelete(");
	}

	@Test
	void generate_allMethodsAreDefault() {
		String source = generateSource("user.yml");

		assertThat(source).contains("default void beforeSave");
		assertThat(source).contains("default void afterSave");
		assertThat(source).contains("default void beforeDelete");
		assertThat(source).contains("default void afterDelete");
	}

	@Test
	void generate_returnsEmptyList_whenHookDisabled() {
		List<GeneratedArtifact> artifacts = generate("post.yml");

		assertThat(artifacts).isEmpty();
	}

	@Test
	void generate_returnsOneArtifact_whenHookEnabled() {
		List<GeneratedArtifact> artifacts = generate("user.yml");

		assertThat(artifacts).hasSize(1);
	}

	@Test
	void generate_outputPathIsInHookBasePackage() {
		List<GeneratedArtifact> artifacts = generate("user.yml");

		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).outputPath().toString())
				.endsWith("user/infrastructure/hook/base/UserHookBase.java");
	}

	private String generateSource(String yamlResource) {
		List<GeneratedArtifact> artifacts = generate(yamlResource);
		assertThat(artifacts).hasSize(1);
		return artifacts.get(0).content();
	}

	private List<GeneratedArtifact> generate(String yamlResource) {
		ServiceConfig config = loadConfig(yamlResource);
		return new HookBaseGenerator().generate(config);
	}

	private ServiceConfig loadConfig(String yamlResource) {
		URL url = getClass().getResource("/service/" + yamlResource);
		assertThat(url).as("Fixture not found: " + yamlResource).isNotNull();
		return ServiceConfigLoader.load(Paths.get(url.getPath()), outputDir);
	}
}
