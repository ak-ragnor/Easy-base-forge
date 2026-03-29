package com.easybase.forge.maven;

import java.io.File;
import java.nio.file.Path;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.easybase.forge.core.config.ConfigException;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.easybase.forge.core.service.config.ServiceConfigLoader;
import com.easybase.forge.core.service.engine.ServiceEngine;
import com.easybase.forge.core.writer.GenerationReport;

/**
 * Generates the Spring Boot service layer (domain model, repository, persistence adapter,
 * base service, service impl, hooks) from a per-entity {@code easybase.yml} spec.
 *
 * <p>Project-level defaults (audit config, soft-delete settings) are read from
 * {@code easybase-config.yaml} when present in the project root.
 *
 * <p>Example usage in a project's {@code pom.xml}:
 * <pre>{@code
 * <plugin>
 *   <groupId>com.easybase</groupId>
 *   <artifactId>easybase-maven-plugin</artifactId>
 *   <version>0.1.0-SNAPSHOT</version>
 *   <executions>
 *     <execution>
 *       <id>generate-user-service</id>
 *       <goals><goal>build-service</goal></goals>
 *       <configuration>
 *         <entityConfigFile>${project.basedir}/easybase.yml</entityConfigFile>
 *       </configuration>
 *     </execution>
 *   </executions>
 * </plugin>
 * }</pre>
 */
@Mojo(name = "build-service", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class BuildServiceMojo extends AbstractMojo {

	/**
	 * Path to the per-entity {@code easybase.yml} configuration file.
	 * Defaults to {@code easybase.yml} in the project base directory.
	 */
	@Parameter(defaultValue = "${project.basedir}/easybase.yml")
	private File entityConfigFile;

	/**
	 * Path to the project-level {@code easybase-config.yaml} configuration file.
	 * Provides shared defaults (audit settings, basePackage) for all entities.
	 * Silently ignored when the file does not exist.
	 */
	@Parameter(defaultValue = "${project.basedir}/easybase-config.yaml")
	private File projectConfigFile;

	/**
	 * Output directory for generated sources.
	 * When not set, the {@code output.directory} from {@code easybase-config.yaml}
	 * or the project's default source directory is used.
	 * Automatically added to the project's compile source roots.
	 */
	@Parameter(defaultValue = "${project.build.sourceDirectory}")
	private File outputDirectory;

	/** Set to {@code true} to skip generation entirely. */
	@Parameter(defaultValue = "false", property = "easybase.skip")
	private boolean skip;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException {
		if (skip) {
			getLog().info("EasyBase Service Builder skipped.");
			return;
		}

		validateParameters();

		getLog().info("EasyBase Service Builder: entity config " + entityConfigFile);
		getLog().info("EasyBase Service Builder: project config " + projectConfigFile);

		ServiceConfig config;

		try {
			Path projectConfigPath = projectConfigFile.exists() ? projectConfigFile.toPath() : null;
			config = ServiceConfigLoader.load(projectConfigPath, entityConfigFile.toPath(), outputDirectory.toPath());
		} catch (ConfigException e) {
			throw new MojoExecutionException("Failed to load EasyBase service config: " + e.getMessage(), e);
		}

		Path resolvedOutput = config.getResolvedOutputDirectory();

		getLog().info("EasyBase Service Builder: output " + resolvedOutput);

		GenerationReport report;

		try {
			report = new ServiceEngine(config).generate();
		} catch (Exception e) {
			throw new MojoExecutionException("EasyBase service generation failed: " + e.getMessage(), e);
		}

		report.created().forEach(p -> getLog().info("[CREATED] " + p));
		report.updated().forEach(p -> getLog().info("[UPDATED] " + p));
		report.skipped().forEach(p -> getLog().info("[SKIPPED] " + p));
		report.errors().forEach(e -> getLog().error("[ERROR]   " + e));

		if (report.hasErrors()) {
			throw new MojoExecutionException(
					"EasyBase service generation completed with errors: " + report.errorSummary());
		}

		project.addCompileSourceRoot(resolvedOutput.toAbsolutePath().toString());

		getLog().info("EasyBase Service Builder: added " + resolvedOutput + " to compile source roots.");
	}

	private void validateParameters() throws MojoExecutionException {
		if (!entityConfigFile.exists()) {
			throw new MojoExecutionException("easybase.yml not found: " + entityConfigFile.getAbsolutePath()
					+ "\nCreate an easybase.yml in your project root with at least:\n"
					+ "  entity: MyEntity\n"
					+ "  basePackage: com.example.app\n"
					+ "  idType: UUID");
		}
	}
}
