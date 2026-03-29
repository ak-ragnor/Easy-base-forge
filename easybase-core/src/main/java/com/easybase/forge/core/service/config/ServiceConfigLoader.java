package com.easybase.forge.core.service.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.easybase.forge.core.config.AuditDefaults;
import com.easybase.forge.core.config.ConfigException;
import com.easybase.forge.core.config.GeneratorConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Loads and validates a per-entity {@link ServiceConfig} from {@code easybase.yml}.
 *
 * <p>When a {@code projectConfigFile} ({@code easybase-config.yaml}) exists, this loader:
 * <ol>
 *   <li>Reads project-level defaults from its {@code service.audit} block.</li>
 *   <li>Inherits {@code basePackage} when not set in the entity config.</li>
 *   <li>Merges audit fields: entity-level non-null values override project defaults.</li>
 * </ol>
 *
 * <p>The {@code projectConfigFile} is silently skipped when it does not exist, allowing
 * the service builder to work standalone without a REST builder config present.
 */
public class ServiceConfigLoader {

	private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();

	private ServiceConfigLoader() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Loads the entity config without a project config (all audit defaults apply).
	 *
	 * @param entityConfigFile path to {@code easybase.yml}
	 * @param outputDirectory  the output directory for generated sources
	 * @return a fully loaded and validated {@link ServiceConfig}
	 */
	public static ServiceConfig load(Path entityConfigFile, Path outputDirectory) {
		return load(null, entityConfigFile, outputDirectory);
	}

	/**
	 * Loads the entity config and merges project-level defaults from the project config.
	 *
	 * @param projectConfigFile path to {@code easybase-config.yaml} (may be {@code null} or non-existent)
	 * @param entityConfigFile  path to {@code easybase.yml}
	 * @param outputDirectory   the output directory for generated sources
	 * @return a fully loaded and validated {@link ServiceConfig}
	 */
	public static ServiceConfig load(Path projectConfigFile, Path entityConfigFile, Path outputDirectory) {
		if (!Files.exists(entityConfigFile)) {
			throw new ConfigException("Entity config file not found: " + entityConfigFile.toAbsolutePath());
		}

		ServiceConfig config = parseEntityConfig(entityConfigFile);
		GeneratorConfig projectConfig = loadProjectConfigIfPresent(projectConfigFile);

		applyProjectDefaults(config, projectConfig);

		ResolvedAuditConfig resolvedAudit = mergeAuditConfig(extractAuditDefaults(projectConfig), config.getAudit());

		config.setResolvedAudit(resolvedAudit);
		config.setResolvedOutputDirectory(outputDirectory);

		validate(config, entityConfigFile);

		return config;
	}

	private static ServiceConfig parseEntityConfig(Path entityConfigFile) {
		try (InputStream in = Files.newInputStream(entityConfigFile)) {
			return YAML_MAPPER.readValue(in, ServiceConfig.class);
		} catch (IOException e) {
			throw new ConfigException("Failed to parse entity config file: " + entityConfigFile, e);
		}
	}

	/**
	 * Parses the project config for defaults extraction only — without running
	 * the full {@link com.easybase.forge.core.config.ConfigLoader} validation
	 * (which requires {@code output.directory} to be set).
	 */
	private static GeneratorConfig loadProjectConfigIfPresent(Path projectConfigFile) {
		if (projectConfigFile == null || !Files.exists(projectConfigFile)) {
			return null;
		}

		try (InputStream in = Files.newInputStream(projectConfigFile)) {
			return YAML_MAPPER.readValue(in, GeneratorConfig.class);
		} catch (IOException e) {
			throw new ConfigException("Failed to parse project config file: " + projectConfigFile, e);
		}
	}

	private static void applyProjectDefaults(ServiceConfig config, GeneratorConfig projectConfig) {
		if (projectConfig == null) {
			return;
		}

		if (config.getBasePackage() == null && projectConfig.getBasePackage() != null) {
			config.setBasePackage(projectConfig.getBasePackage());
		}
	}

	private static AuditDefaults extractAuditDefaults(GeneratorConfig projectConfig) {
		if (projectConfig == null) {
			return new AuditDefaults();
		}

		return projectConfig.getService().getAudit();
	}

	private static ResolvedAuditConfig mergeAuditConfig(AuditDefaults defaults, AuditConfig override) {
		boolean enabled = defaults.isEnabled();
		String auditorType = defaults.getAuditorType();
		boolean softDelete = defaults.isSoftDelete();
		String softDeleteColumn = defaults.getSoftDeleteColumn();

		if (override != null) {
			if (override.getEnabled() != null) {
				enabled = override.getEnabled();
			}
			if (override.getAuditorType() != null) {
				auditorType = override.getAuditorType();
			}
			if (override.getSoftDelete() != null) {
				softDelete = override.getSoftDelete();
			}
			if (override.getSoftDeleteColumn() != null) {
				softDeleteColumn = override.getSoftDeleteColumn();
			}
		}

		return new ResolvedAuditConfig(enabled, auditorType, softDelete, softDeleteColumn);
	}

	private static void validate(ServiceConfig config, Path entityConfigFile) {
		if (config.getEntity() == null || config.getEntity().isBlank()) {
			throw new ConfigException("'entity' is required in " + entityConfigFile);
		}

		if (config.getBasePackage() == null || config.getBasePackage().isBlank()) {
			throw new ConfigException(
					"'basePackage' is required. Set it in " + entityConfigFile + " or in easybase-config.yaml");
		}

		if (config.getResolvedOutputDirectory() == null) {
			throw new ConfigException("Output directory must be provided");
		}
	}
}
