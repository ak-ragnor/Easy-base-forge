package com.easybase.forge.maven;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.easybase.forge.common.config.ConfigException;
import com.easybase.forge.common.io.GenerationReport;
import com.easybase.forge.service.config.ServiceConfig;
import com.easybase.forge.service.config.ServiceConfigLoader;
import com.easybase.forge.service.engine.ServiceEngine;

/**
 * Generates the Spring Boot service layer (domain model, repository, persistence adapter,
 * base service, service impl, hooks) from a {@code service.yml} spec.
 *
 * <p>Example usage in a project's {@code pom.xml}:
 * <pre>{@code
 * <plugin>
 *   <groupId>com.easybase</groupId>
 *   <artifactId>easybase-maven-plugin</artifactId>
 *   <version>0.1.0-SNAPSHOT</version>
 *   <executions>
 *     <execution>
 *       <id>generate-service</id>
 *       <goals><goal>build-service</goal></goals>
 *       <configuration>
 *         <serviceConfigFile>${project.basedir}/src/main/resources/service.yml</serviceConfigFile>
 *       </configuration>
 *     </execution>
 *   </executions>
 * </plugin>
 * }</pre>
 */
@Mojo(name = "build-service", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class BuildServiceMojo extends AbstractMojo {

	/**
	 * Path to the {@code service.yml} configuration file.
	 * Defaults to {@code service.yml} in the project base directory.
	 */
	@Parameter(defaultValue = "${project.basedir}/src/main/resources/service.yml")
	private File serviceConfigFile;

	/**
	 * Path to the project-level {@code easybase-config.yaml} configuration file.
	 * Provides shared defaults (basePackage, generate options) for all entities.
	 * Silently ignored when the file does not exist.
	 */
	@Parameter(defaultValue = "${project.basedir}/easybase-config.yaml")
	private File projectConfigFile;

	/**
	 * Output directory for generated sources.
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

		getLog().info("EasyBase Service Builder: service config " + serviceConfigFile);
		getLog().info("EasyBase Service Builder: project config " + projectConfigFile);

		List<ServiceConfig> configs;

		try {
			Path projectConfigPath = null;

			if (projectConfigFile.exists()) {
				projectConfigPath = projectConfigFile.toPath();
			}

			configs = ServiceConfigLoader.loadAll(
					projectConfigPath, serviceConfigFile.toPath(), outputDirectory.toPath());
		} catch (ConfigException e) {
			throw new MojoExecutionException("Failed to load EasyBase service config: " + e.getMessage(), e);
		}

		Path resolvedOutput = configs.get(0).getResolvedOutputDirectory();

		getLog().info("EasyBase Service Builder: output " + resolvedOutput);

		for (ServiceConfig config : configs) {
			getLog().info("EasyBase Service Builder: generating entity " + config.getEntity() + " (module: "
					+ config.getModuleName() + ")");

			GenerationReport report;

			try {
				report = new ServiceEngine(config).generate();
			} catch (Exception e) {
				throw new MojoExecutionException(
						"EasyBase service generation failed for entity " + config.getEntity() + ": " + e.getMessage(),
						e);
			}

			report.created().forEach(p -> getLog().info("[CREATED] " + p));
			report.updated().forEach(p -> getLog().info("[UPDATED] " + p));
			report.skipped().forEach(p -> getLog().info("[SKIPPED] " + p));
			report.errors().forEach(e -> getLog().error("[ERROR]   " + e));

			if (report.hasErrors()) {
				throw new MojoExecutionException("EasyBase service generation completed with errors for entity "
						+ config.getEntity() + ": " + report.errorSummary());
			}
		}

		project.addCompileSourceRoot(resolvedOutput.toAbsolutePath().toString());
		getLog().info("EasyBase Service Builder: added " + resolvedOutput + " to compile source roots.");
	}

	private void validateParameters() throws MojoExecutionException {
		if (!serviceConfigFile.exists()) {
			throw new MojoExecutionException("service.yml not found: " + serviceConfigFile.getAbsolutePath()
					+ "\nCreate a service.yml in your project root with at least:\n"
					+ "  basePackage: com.example.app\n"
					+ "  modules:\n"
					+ "    - name: mymodule\n"
					+ "      entities:\n"
					+ "        - name: MyEntity\n");
		}
	}
}
