package com.easybase.forge.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

/**
 * DSL extension for the EasyBase Forge Gradle plugin.
 *
 * <p>Configure in {@code build.gradle}:
 * <pre>{@code
 * easybase {
 *     specFile = file('src/main/resources/api.yaml')
 *     configFile = file('easybase-config.yaml')  // optional, this is the default
 * }
 * }</pre>
 */
public abstract class EasyBaseExtension {

	/** Path to the OpenAPI YAML/JSON specification file. Required. */
	public abstract RegularFileProperty getSpecFile();

	/**
	 * Path to the {@code easybase-config.yaml} configuration file.
	 * Defaults to {@code easybase-config.yaml} in the project directory.
	 */
	public abstract RegularFileProperty getConfigFile();

	/**
	 * Override output directory for generated sources.
	 * If not set, uses {@code output.directory} from the config file.
	 */
	public abstract DirectoryProperty getOutputDirectory();

	/** Set to {@code true} to skip code generation. Default: {@code false}. */
	public abstract Property<Boolean> getSkip();
}
