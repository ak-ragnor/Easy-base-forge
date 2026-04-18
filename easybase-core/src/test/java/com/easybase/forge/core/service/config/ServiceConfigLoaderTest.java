package com.easybase.forge.core.service.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.easybase.forge.core.config.ConfigException;

class ServiceConfigLoaderTest {

	@TempDir
	Path outputDir;

	@Test
	void loadEntityOnly_appliesAuditDefaults() {
		ServiceConfig config = loadConfig("post.yml");

		assertThat(config.getResolvedAudit().enabled()).isFalse();
	}

	@Test
	void loadEntityOnly_userYaml_softDeleteEnabled() {
		ServiceConfig config = loadConfig("user.yml");

		assertThat(config.getResolvedSoftDelete().enabled()).isTrue();
	}

	@Test
	void loadEntityOnly_userYaml_auditorTypeIsUuid() {
		ServiceConfig config = loadConfig("user.yml");

		assertThat(config.getResolvedAudit().auditorType()).isEqualTo("UUID");
	}

	@Test
	void loadEntityOnly_setsEntityName() {
		ServiceConfig config = loadConfig("user.yml");

		assertThat(config.getEntity()).isEqualTo("User");
	}

	@Test
	void loadEntityOnly_setsBasePackage() {
		ServiceConfig config = loadConfig("user.yml");

		assertThat(config.getBasePackage()).isEqualTo("com.example.app");
	}

	@Test
	void loadEntityOnly_setsResolvedOutputDirectory() {
		ServiceConfig config = loadConfig("user.yml");

		assertThat(config.getResolvedOutputDirectory()).isEqualTo(outputDir);
	}

	@Test
	void loadWithProjectConfig_inheritsBasePackageFromProject() throws IOException {
		Path entityConfig = writeEntityYaml("entity: Widget\n");
		Path projectConfig = writeProjectYaml("basePackage: com.example.project\n");

		ServiceConfig config = ServiceConfigLoader.load(projectConfig, entityConfig, outputDir);

		assertThat(config.getBasePackage()).isEqualTo("com.example.project");
	}

	@Test
	void loadWithProjectConfig_entityBasePackageTakesPrecedence() throws IOException {
		Path entityConfig = writeEntityYaml("entity: Widget\nbasePackage: com.entity.pkg\n");
		Path projectConfig = writeProjectYaml("basePackage: com.project.pkg\n");

		ServiceConfig config = ServiceConfigLoader.load(projectConfig, entityConfig, outputDir);

		assertThat(config.getBasePackage()).isEqualTo("com.entity.pkg");
	}

	@Test
	void loadWithProjectConfig_inheritsAuthorsFromProjectGenerate() throws IOException {
		Path entityConfig = writeEntityYaml("entity: Widget\nbasePackage: com.example.app\n");
		Path projectConfig = writeProjectYaml("basePackage: com.example.app\ngenerate:\n  authors:\n    - EasyBase\n");

		ServiceConfig config = ServiceConfigLoader.load(projectConfig, entityConfig, outputDir);

		assertThat(config.getResolvedAuthors()).contains("EasyBase");
	}

	@Test
	void loadWithProjectConfig_inheritsAddGeneratedAnnotationFromProject() throws IOException {
		Path entityConfig = writeEntityYaml("entity: Widget\nbasePackage: com.example.app\n");
		Path projectConfig =
				writeProjectYaml("basePackage: com.example.app\ngenerate:\n  addGeneratedAnnotation: true\n");

		ServiceConfig config = ServiceConfigLoader.load(projectConfig, entityConfig, outputDir);

		assertThat(config.isResolvedAddGeneratedAnnotation()).isTrue();
	}

	@Test
	void load_throwsConfigException_whenEntityNameMissing() throws IOException {
		Path entityConfig = writeEntityYaml("basePackage: com.example.app\n");

		assertThatThrownBy(() -> ServiceConfigLoader.load(entityConfig, outputDir))
				.isInstanceOf(ConfigException.class)
				.hasMessageContaining("entity");
	}

	@Test
	void load_throwsConfigException_whenBasePackageMissingEverywhere() throws IOException {
		Path entityConfig = writeEntityYaml("entity: Widget\n");

		assertThatThrownBy(() -> ServiceConfigLoader.load(entityConfig, outputDir))
				.isInstanceOf(ConfigException.class)
				.hasMessageContaining("basePackage");
	}

	@Test
	void load_throwsConfigException_whenEntityConfigFileDoesNotExist() {
		Path nonExistent = outputDir.resolve("does-not-exist.yml");

		assertThatThrownBy(() -> ServiceConfigLoader.load(nonExistent, outputDir))
				.isInstanceOf(ConfigException.class)
				.hasMessageContaining("not found");
	}

	@Test
	void load_silentlyIgnoresMissingProjectConfig() throws IOException {
		Path entityConfig = writeEntityYaml("entity: Widget\nbasePackage: com.example.app\n");
		Path nonExistentProject = outputDir.resolve("no-project-config.yaml");

		ServiceConfig config = ServiceConfigLoader.load(nonExistentProject, entityConfig, outputDir);

		assertThat(config.getEntity()).isEqualTo("Widget");
	}

	private ServiceConfig loadConfig(String yamlResource) {
		URL url = getClass().getResource("/service/" + yamlResource);
		assertThat(url).as("Fixture not found: " + yamlResource).isNotNull();
		Path yamlPath = Paths.get(url.getPath());
		return ServiceConfigLoader.load(yamlPath, outputDir);
	}

	private Path writeEntityYaml(String content) throws IOException {
		Path file = outputDir.resolve("easybase.yml");
		Files.writeString(file, content);
		return file;
	}

	private Path writeProjectYaml(String content) throws IOException {
		Path file = outputDir.resolve("easybase-config.yaml");
		Files.writeString(file, content);
		return file;
	}
}
