package com.easybase.forge.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.easybase.forge.core.config.ConfigException;
import com.easybase.forge.core.config.ConfigLoader;
import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.engine.GeneratorEngine;
import com.easybase.forge.core.writer.GenerationReport;

/**
 * Generates Spring Boot REST layer (controllers, delegates, DTOs) from an OpenAPI spec.
 *
 * <p>Example usage in a project's {@code pom.xml}:
 * <pre>{@code
 * <plugin>
 *   <groupId>com.easybase</groupId>
 *   <artifactId>easybase-maven-plugin</artifactId>
 *   <version>0.1.0-SNAPSHOT</version>
 *   <configuration>
 *     <specFile>${project.basedir}/src/main/resources/openapi.yaml</specFile>
 *   </configuration>
 *   <executions>
 *     <execution>
 *       <goals><goal>generate</goal></goals>
 *     </execution>
 *   </executions>
 * </plugin>
 * }</pre>
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GenerateMojo extends AbstractMojo {

	private static final long POST_GENERATE_TIMEOUT_MINUTES = 10L;

	/** Path to the OpenAPI YAML or JSON specification file. Required. */
	@Parameter(required = true)
	private File specFile;

	/**
	 * Path to the {@code easybase-config.yaml} configuration file.
	 * Defaults to {@code easybase-config.yaml} in the project base directory.
	 */
	@Parameter(defaultValue = "${project.basedir}/easybase-config.yaml")
	private File configFile;

	/**
	 * Output directory for generated sources.
	 * If not set, falls back to {@code output.directory} in easybase-config.yaml.
	 * Automatically added to the project's compile source roots.
	 */
	@Parameter
	private File outputDirectory;

	/** Set to {@code true} to skip generation entirely. */
	@Parameter(defaultValue = "false", property = "easybase.skip")
	private boolean skip;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException {
		if (skip) {
			getLog().info("EasyBase generation skipped.");
			return;
		}

		validateParameters();

		getLog().info("EasyBase: generating from " + specFile);
		getLog().info("EasyBase: config     " + configFile);

		GeneratorConfig config;

		try {
			Path outputOverride = outputDirectory != null ? outputDirectory.toPath() : null;
			config = ConfigLoader.load(configFile.toPath(), outputOverride);
		} catch (ConfigException e) {
			throw new MojoExecutionException("Failed to load EasyBase config: " + e.getMessage(), e);
		}

		Path resolvedOutput = config.getResolvedOutputDirectory();

		getLog().info("EasyBase: output     " + resolvedOutput);

		GenerationReport report;

		try {
			report = new GeneratorEngine(config).generate(specFile.toPath());
		} catch (Exception e) {
			throw new MojoExecutionException("EasyBase generation failed: " + e.getMessage(), e);
		}

		report.created().forEach(p -> getLog().info("[CREATED] " + p));
		report.updated().forEach(p -> getLog().info("[UPDATED] " + p));
		report.skipped().forEach(p -> getLog().info("[SKIPPED] " + p));
		report.errors().forEach(e -> getLog().error("[ERROR]   " + e));

		if (report.hasErrors()) {
			throw new MojoExecutionException("EasyBase generation completed with errors: " + report.errorSummary());
		}

		project.addCompileSourceRoot(resolvedOutput.toAbsolutePath().toString());

		getLog().info("EasyBase: added " + resolvedOutput + " to compile source roots.");

		String postCmd = config.getGenerate().getPostGenerateCommand();

		if (postCmd != null && !postCmd.isBlank()) {
			getLog().info("EasyBase: running post-generate command: " + postCmd);
			runPostGenerateCommand(postCmd, resolvedOutput);
		}
	}

	void runPostGenerateCommand(String command, Path workingDir) throws MojoExecutionException {
		try {
			Process process = new ProcessBuilder("bash", "-c", command)
					.directory(workingDir.toFile())
					.inheritIO()
					.start();

			boolean finished = process.waitFor(POST_GENERATE_TIMEOUT_MINUTES, TimeUnit.MINUTES);

			if (!finished) {
				process.destroyForcibly();
				throw new MojoExecutionException("Post-generate command timed out after "
						+ POST_GENERATE_TIMEOUT_MINUTES + " minutes: " + command);
			}

			int exit = process.exitValue();

			if (exit != 0) {
				throw new MojoExecutionException("Post-generate command failed (exit " + exit + "): " + command);
			}
		} catch (IOException | InterruptedException e) {
			throw new MojoExecutionException("Failed to run post-generate command: " + command, e);
		}
	}

	private void validateParameters() throws MojoExecutionException {
		if (specFile == null || !specFile.exists()) {
			throw new MojoExecutionException("specFile not found: "
					+ (specFile != null ? specFile.getAbsolutePath() : "(null)")
					+ "\nConfigure it in your pom.xml:\n"
					+ "  <configuration>\n"
					+ "    <specFile>${project.basedir}/src/main/resources/openapi.yaml</specFile>\n"
					+ "  </configuration>");
		}

		if (!configFile.exists()) {
			throw new MojoExecutionException("easybase-config.yaml not found: " + configFile.getAbsolutePath()
					+ "\nCreate an easybase-config.yaml in your project root with at least:\n"
					+ "  basePackage: com.example.api");
		}
	}
}
