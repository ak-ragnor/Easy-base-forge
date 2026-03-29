package com.easybase.forge.cli;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import com.easybase.forge.core.config.ConfigException;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.easybase.forge.core.service.config.ServiceConfigLoader;
import com.easybase.forge.core.service.engine.ServiceEngine;
import com.easybase.forge.core.writer.GenerationReport;
import com.easybase.forge.core.writer.GenerationUnit;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * CLI command for generating a Spring Boot service layer from a per-entity {@code easybase.yml}.
 *
 * <p>Usage:
 * <pre>
 *   easybase build-service easybase.yml
 *   easybase build-service easybase.yml -o src/main/java
 *   easybase build-service easybase.yml -p easybase-config.yaml --dry-run
 * </pre>
 */
@Command(
		name = "build-service",
		description = "Generate Spring Boot service layer from an easybase.yml entity spec.",
		mixinStandardHelpOptions = true)
public class BuildServiceCommand implements Callable<Integer> {

	@Parameters(index = "0", description = "Path to the easybase.yml entity config file.")
	private File entityConfigFile;

	@Option(
			names = {"-p", "--project-config"},
			description = "Path to easybase-config.yaml. Provides shared defaults (audit, basePackage)."
					+ " Default: ./easybase-config.yaml",
			defaultValue = "easybase-config.yaml")
	private File projectConfigFile;

	@Option(
			names = {"-o", "--output"},
			description = "Output directory for generated sources. Default: src/main/java",
			defaultValue = "src/main/java")
	private File outputDirectory;

	@Option(
			names = {"--dry-run"},
			description = "Print what would be generated without writing any files.")
	private boolean dryRun;

	@Override
	public Integer call() {
		if (!entityConfigFile.exists()) {
			System.err.println("Error: entity config file not found: " + entityConfigFile.getAbsolutePath());
			System.err.println("Create an easybase.yml with at least:");
			System.err.println("  entity: MyEntity");
			System.err.println("  basePackage: com.example.app");

			return 1;
		}

		ServiceConfig config;

		try {
			Path projectConfigPath = projectConfigFile.exists() ? projectConfigFile.toPath() : null;
			config = ServiceConfigLoader.load(projectConfigPath, entityConfigFile.toPath(), outputDirectory.toPath());
		} catch (ConfigException e) {
			System.err.println("Error: " + e.getMessage());
			return 1;
		}

		if (dryRun) {
			return runDryRun(config);
		}

		try {
			GenerationReport report = new ServiceEngine(config).generate();
			printReport(report);

			if (report.hasErrors()) {
				return 1;
			}

			return 0;
		} catch (Exception e) {
			System.err.println("Error: service generation failed: " + e.getMessage());
			return 1;
		}
	}

	private int runDryRun(ServiceConfig config) {
		System.out.println("[DRY RUN] Would generate the following files:");

		try {
			List<GenerationUnit> units = new ServiceEngine(config).plan();

			for (GenerationUnit unit : units) {
				String action = unit.overwrite() ? "CREATE/UPDATE" : "CREATE (skip if exists)";
				System.out.printf("  [%-20s] %s%n", action, unit.outputPath());
			}

			System.out.println("[DRY RUN] Total: " + units.size() + " file(s).");
			return 0;
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			return 1;
		}
	}

	private static void printReport(GenerationReport report) {
		report.created().forEach(p -> System.out.println("[CREATED] " + p));
		report.updated().forEach(p -> System.out.println("[UPDATED] " + p));
		report.skipped().forEach(p -> System.out.println("[SKIPPED] " + p));
		report.errors().forEach(e -> System.err.println("[ERROR]   " + e));

		System.out.printf(
				"%nDone: %d created, %d updated, %d skipped, %d errors.%n",
				report.created().size(),
				report.updated().size(),
				report.skipped().size(),
				report.errors().size());
	}
}
