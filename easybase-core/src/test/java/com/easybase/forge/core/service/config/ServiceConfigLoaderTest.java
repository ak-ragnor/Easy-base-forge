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

/**
 * Tests for {@link ServiceConfigLoader}: YAML parsing, project-defaults merging,
 * basePackage inheritance, and validation error reporting.
 */
class ServiceConfigLoaderTest {

	@TempDir
	Path outputDir;

	// -------------------------------------------------------------------------
	// Load entity config only (no project config)
	// -------------------------------------------------------------------------

	@Test
	void loadEntityOnly_appliesAuditDefaults() {
		ServiceConfig config = loadConfig("post.yml");

		// post.yml disables audit entirely — this tests defaults inside the entity file
		assertThat(config.getResolvedAudit().enabled()).isFalse();
	}

	@Test
	void loadEntityOnly_userYaml_softDeleteEnabled() {
		ServiceConfig config = loadConfig("user.yml");

		assertThat(config.getResolvedAudit().softDelete()).isTrue();
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

	// -------------------------------------------------------------------------
	// Load with project config — basePackage inheritance
	// -------------------------------------------------------------------------

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

	// -------------------------------------------------------------------------
	// Load with project config — audit merging
	// -------------------------------------------------------------------------

	@Test
	void loadWithProjectConfig_entityAuditOverridesProjectSoftDelete() throws IOException {
		Path entityConfig =
				writeEntityYaml("entity: Widget\nbasePackage: com.example.app\naudit:\n  softDelete: false\n");
		Path projectConfig =
				writeProjectYaml("basePackage: com.example.app\nservice:\n  audit:\n    softDelete: true\n");

		ServiceConfig config = ServiceConfigLoader.load(projectConfig, entityConfig, outputDir);

		assertThat(config.getResolvedAudit().softDelete()).isFalse();
	}

	@Test
	void loadWithProjectConfig_projectAuditAppliesWhenEntityHasNoOverride() throws IOException {
		Path entityConfig = writeEntityYaml("entity: Widget\nbasePackage: com.example.app\n");
		Path projectConfig =
				writeProjectYaml("basePackage: com.example.app\nservice:\n  audit:\n    auditorType: \"Long\"\n");

		ServiceConfig config = ServiceConfigLoader.load(projectConfig, entityConfig, outputDir);

		assertThat(config.getResolvedAudit().auditorType()).isEqualTo("Long");
	}

	@Test
	void loadWithProjectConfig_entityAuditorTypeOverridesProject() throws IOException {
		Path entityConfig =
				writeEntityYaml("entity: Widget\nbasePackage: com.example.app\naudit:\n  auditorType: \"String\"\n");
		Path projectConfig =
				writeProjectYaml("basePackage: com.example.app\nservice:\n  audit:\n    auditorType: \"Long\"\n");

		ServiceConfig config = ServiceConfigLoader.load(projectConfig, entityConfig, outputDir);

		assertThat(config.getResolvedAudit().auditorType()).isEqualTo("String");
	}

	// -------------------------------------------------------------------------
	// Validation errors
	// -------------------------------------------------------------------------

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

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

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
