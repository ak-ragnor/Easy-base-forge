package com.easybase.forge.gradle;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import com.easybase.forge.core.config.ConfigException;
import com.easybase.forge.core.config.ConfigLoader;
import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.engine.GeneratorEngine;
import com.easybase.forge.core.writer.GenerationReport;

/**
 * Gradle task that generates Spring REST layer from an OpenAPI specification.
 *
 * <p>Registered as {@code easybaseGenerate} by {@link EasyBasePlugin}.
 * Automatically wired as a dependency of {@code compileJava}.
 */
@CacheableTask
public abstract class GenerateTask extends DefaultTask {

	private static final long POST_GENERATE_TIMEOUT_MINUTES = 10L;

	/** Path to the OpenAPI YAML/JSON specification file. */
	@InputFile
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract RegularFileProperty getSpecFile();

	/** Path to the easybase config file. */
	@InputFile
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract RegularFileProperty getConfigFile();

	/** Override output directory. Optional — falls back to config file's output.directory. */
	@Optional
	@Input
	public abstract Property<String> getOutputDirectoryOverride();

	@TaskAction
	public void generate() {
		Logger log = getLogger();

		Path specPath = getSpecFile().get().getAsFile().toPath();
		Path configPath = getConfigFile().get().getAsFile().toPath();

		log.lifecycle("EasyBase: generating from {}", specPath);
		log.lifecycle("EasyBase: config     {}", configPath);

		GeneratorConfig config;

		try {
			Path outputOverride = getOutputDirectoryOverride().isPresent()
					? Path.of(getOutputDirectoryOverride().get())
					: null;
			config = ConfigLoader.load(configPath, outputOverride);
		} catch (ConfigException e) {
			throw new GradleException("Failed to load EasyBase config: " + e.getMessage(), e);
		}

		Path resolvedOutput = config.getResolvedOutputDirectory();

		log.lifecycle("EasyBase: output     {}", resolvedOutput);

		GenerationReport report;

		try {
			report = new GeneratorEngine(config).generate(specPath);
		} catch (Exception e) {
			throw new GradleException("EasyBase generation failed: " + e.getMessage(), e);
		}

		report.created().forEach(p -> log.lifecycle("[CREATED] {}", p));
		report.updated().forEach(p -> log.lifecycle("[UPDATED] {}", p));
		report.skipped().forEach(p -> log.lifecycle("[SKIPPED] {}", p));
		report.errors().forEach(e -> log.error("[ERROR]   {}", e));

		if (report.hasErrors()) {
			throw new GradleException("EasyBase generation completed with errors: " + report.errorSummary());
		}

		String postCmd = config.getGenerate().getPostGenerateCommand();

		if (postCmd != null && !postCmd.isBlank()) {
			log.lifecycle("EasyBase: running post-generate command: {}", postCmd);
			runPostGenerateCommand(postCmd, resolvedOutput);
		}
	}

	private void runPostGenerateCommand(String command, Path workingDir) {
		try {
			Process process = new ProcessBuilder("bash", "-c", command)
					.directory(workingDir.toFile())
					.inheritIO()
					.start();

			boolean finished = process.waitFor(POST_GENERATE_TIMEOUT_MINUTES, TimeUnit.MINUTES);

			if (!finished) {
				process.destroyForcibly();
				throw new GradleException("Post-generate command timed out after " + POST_GENERATE_TIMEOUT_MINUTES
						+ " minutes: " + command);
			}

			int exit = process.exitValue();

			if (exit != 0) {
				throw new GradleException("Post-generate command failed (exit " + exit + "): " + command);
			}
		} catch (IOException | InterruptedException e) {
			throw new GradleException("Failed to run post-generate command: " + command, e);
		}
	}
}
