package com.easybase.forge.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Loads and validates {@link GeneratorConfig} from a YAML file.
 */
public class ConfigLoader {

	private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();

	private ConfigLoader() {
		throw new UnsupportedOperationException("Utility class");
	}

	/** Loads config resolving {@code output.directory} relative to the config file location. */
	public static GeneratorConfig load(Path configFile) {
		return load(configFile, null);
	}

	/**
	 * Loads config and applies {@code outputDirOverride} before validation.
	 * Used by the Maven Mojo and CLI so they can supply the output directory
	 * without requiring it in the config file.
	 */
	public static GeneratorConfig load(Path configFile, Path outputDirOverride) {
		if (!Files.exists(configFile)) {
			throw new ConfigException("Config file not found: " + configFile.toAbsolutePath());
		}

		final GeneratorConfig config;
		try (InputStream in = Files.newInputStream(configFile)) {
			config = YAML_MAPPER.readValue(in, GeneratorConfig.class);
		} catch (IOException e) {
			throw new ConfigException("Failed to parse config file: " + configFile, e);
		}

		if (outputDirOverride != null) {
			config.withOutputDirectory(outputDirOverride);
		} else if (config.getOutput().getDirectory() != null) {
			final Path outputDir = configFile
					.getParent()
					.resolve(config.getOutput().getDirectory())
					.normalize();
			config.withOutputDirectory(outputDir);
		}

		validate(config, configFile);
		return config;
	}

	private static void validate(GeneratorConfig config, Path configFile) {
		if (config.getBasePackage() == null || config.getBasePackage().isBlank()) {
			throw new ConfigException("'basePackage' is required in " + configFile);
		}
		if (config.getOutput().getDirectory() == null && config.getResolvedOutputDirectory() == null) {
			throw new ConfigException("'output.directory' is required in " + configFile);
		}
	}
}
