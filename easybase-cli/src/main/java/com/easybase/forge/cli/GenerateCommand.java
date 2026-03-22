package com.easybase.forge.cli;

import com.easybase.forge.core.config.ConfigException;
import com.easybase.forge.core.config.ConfigLoader;
import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.engine.GeneratorEngine;
import com.easybase.forge.core.writer.GenerationReport;
import com.easybase.forge.core.writer.GenerationPlan;
import com.easybase.forge.core.writer.GenerationUnit;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "generate",
        description = "Generate Spring Boot REST layer from an OpenAPI spec.",
        mixinStandardHelpOptions = true
)
public class GenerateCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Path to the OpenAPI YAML/JSON spec file.")
    private File specFile;

    @Option(
            names = {"-c", "--config"},
            description = "Path to easybase-config.yaml. Default: ./easybase-config.yaml",
            defaultValue = "easybase-config.yaml"
    )
    private File configFile;

    @Option(
            names = {"-o", "--output"},
            description = "Output directory. Overrides output.directory in the config file."
    )
    private File outputDirectory;

    @Option(
            names = {"--dry-run"},
            description = "Print what would be generated without writing any files."
    )
    private boolean dryRun;

    @Override
    public Integer call() {
        // Validate inputs
        if (!specFile.exists()) {
            System.err.println("Error: spec file not found: " + specFile.getAbsolutePath());
            return 1;
        }
        if (!configFile.exists()) {
            System.err.println("Error: config file not found: " + configFile.getAbsolutePath());
            System.err.println("Create an easybase-config.yaml with at least: basePackage: com.example.api");
            return 1;
        }

        GeneratorConfig config;
        try {
            Path outputOverride = outputDirectory != null ? outputDirectory.toPath() : null;
            config = ConfigLoader.load(configFile.toPath(), outputOverride);
        } catch (ConfigException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }

        if (dryRun) {
            return runDryRun(config);
        }

        try {
            GenerationReport report = new GeneratorEngine(config).generate(specFile.toPath());
            printReport(report);
            return report.hasErrors() ? 1 : 0;
        } catch (Exception e) {
            System.err.println("Error: generation failed: " + e.getMessage());
            return 1;
        }
    }

    private int runDryRun(GeneratorConfig config) {
        System.out.println("[DRY RUN] Would generate the following files:");
        try {
            var spec = new GeneratorEngine(config).parse(specFile.toPath());
            List<GenerationUnit> units = new GenerationPlan().build(spec.resources(), config);
            units.forEach(u -> {
                String action = u.overwrite() ? "CREATE/UPDATE" : "CREATE (skip if exists)";
                System.out.printf("  [%-20s] %s%n", action, u.outputPath());
            });
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

        System.out.printf("%nDone: %d created, %d updated, %d skipped, %d errors.%n",
                report.created().size(), report.updated().size(),
                report.skipped().size(), report.errors().size());
    }
}
