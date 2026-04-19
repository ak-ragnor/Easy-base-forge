package com.easybase.forge.cli;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import com.easybase.forge.common.config.ConfigException;
import com.easybase.forge.common.io.GenerationReport;
import com.easybase.forge.common.io.GenerationUnit;
import com.easybase.forge.service.config.ServiceConfig;
import com.easybase.forge.service.config.ServiceConfigLoader;
import com.easybase.forge.service.engine.ServiceEngine;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
		name = "build-service",
		description = "Generate Spring Boot service layer from a service.yml spec.",
		mixinStandardHelpOptions = true)
public class BuildServiceCommand implements Callable<Integer> {

	@Parameters(
			index = "0",
			description = "Path to the service.yml config file.",
			defaultValue = "src/main/resources/service.yml")
	private File serviceConfigFile;

	@Option(
			names = {"-p", "--project-config"},
			description = "Path to easybase-config.yaml. Provides shared defaults (basePackage)."
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
		if (!serviceConfigFile.exists()) {
			System.err.println("Error: service config file not found: " + serviceConfigFile.getAbsolutePath());
			System.err.println("Create a service.yml with at least:");
			System.err.println("  basePackage: com.example.app");
			System.err.println("  modules:");
			System.err.println("    - name: mymodule");
			System.err.println("      entities:");
			System.err.println("        - name: MyEntity");
			return 1;
		}

		List<ServiceConfig> configs;

		try {
			Path projectConfigPath = null;

			if (projectConfigFile.exists()) {
				projectConfigPath = projectConfigFile.toPath();
			}

			configs = ServiceConfigLoader.loadAll(
					projectConfigPath, serviceConfigFile.toPath(), outputDirectory.toPath());
		} catch (ConfigException e) {
			System.err.println("Error: " + e.getMessage());
			return 1;
		}

		if (dryRun) {
			return runDryRun(configs);
		}

		int exitCode = 0;

		for (ServiceConfig config : configs) {
			try {
				GenerationReport report = new ServiceEngine(config).generate();

				printReport(config, report);

				if (report.hasErrors()) {
					exitCode = 1;
				}
			} catch (Exception e) {
				System.err.println(
						"Error: service generation failed for entity " + config.getEntity() + ": " + e.getMessage());
				exitCode = 1;
			}
		}

		return exitCode;
	}

	private int runDryRun(List<ServiceConfig> configs) {
		System.out.println("[DRY RUN] Would generate the following files:");

		int total = 0;

		for (ServiceConfig config : configs) {
			try {
				List<GenerationUnit> units = new ServiceEngine(config).plan();

				for (GenerationUnit unit : units) {
					String action = unit.overwrite() ? "CREATE/UPDATE" : "CREATE (skip if exists)";
					System.out.printf("  [%-20s] %s%n", action, unit.outputPath());
				}

				total += units.size();
			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
				return 1;
			}
		}

		System.out.println("[DRY RUN] Total: " + total + " file(s).");
		return 0;
	}

	private static void printReport(ServiceConfig config, GenerationReport report) {
		report.created().forEach(p -> System.out.println("[CREATED] " + p));
		report.updated().forEach(p -> System.out.println("[UPDATED] " + p));
		report.skipped().forEach(p -> System.out.println("[SKIPPED] " + p));
		report.errors().forEach(e -> System.err.println("[ERROR]   " + e));

		System.out.printf(
				"%nEntity %s: %d created, %d updated, %d skipped, %d errors.%n",
				config.getEntity(),
				report.created().size(),
				report.updated().size(),
				report.skipped().size(),
				report.errors().size());
	}
}
