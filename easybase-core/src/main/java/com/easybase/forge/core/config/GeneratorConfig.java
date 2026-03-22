package com.easybase.forge.core.config;

import java.nio.file.Path;

/**
 * Root configuration object parsed from {@code easybase-config.yaml}.
 *
 * <p>Example YAML:
 * <pre>
 * basePackage: com.example.api
 * output:
 *   directory: ../api-rest/src/main/java
 *   layout: MULTI_MODULE
 * structure:
 *   controller:
 *     package: "{basePackage}.{resource}.controller"
 *     basePackage: "{basePackage}.{resource}.controller.base"
 *   delegate:
 *     package: "{basePackage}.{resource}.delegate"
 *   dto:
 *     package: "{basePackage}.{resource}.dto"
 * generate:
 *   delegateImpl: false
 *   responseEntityWrapping: ALWAYS
 *   beanValidation: true
 *   pagination: NONE
 * </pre>
 */
public class GeneratorConfig {

    private String basePackage;
    private OutputConfig output = new OutputConfig();
    private StructureConfig structure = new StructureConfig();
    private GenerateOptions generate = new GenerateOptions();

    /** Resolved output directory (may be set programmatically by Maven/CLI). */
    private Path resolvedOutputDirectory;

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public OutputConfig getOutput() {
        return output;
    }

    public void setOutput(OutputConfig output) {
        this.output = output;
    }

    public StructureConfig getStructure() {
        return structure;
    }

    public void setStructure(StructureConfig structure) {
        this.structure = structure;
    }

    public GenerateOptions getGenerate() {
        return generate;
    }

    public void setGenerate(GenerateOptions generate) {
        this.generate = generate;
    }

    public Path getResolvedOutputDirectory() {
        return resolvedOutputDirectory;
    }

    /** Override the output directory (used by Maven Mojo or CLI). */
    public GeneratorConfig withOutputDirectory(Path outputDirectory) {
        this.resolvedOutputDirectory = outputDirectory;
        return this;
    }

    /**
     * Resolve a package pattern by substituting {@code {basePackage}}, {@code {resource}},
     * and {@code {Resource}} placeholders.
     */
    public String resolvePackage(String pattern, String resourceName) {
        String pascalCase = toPascalCase(resourceName);
        return pattern
                .replace("{basePackage}", basePackage)
                .replace("{Resource}", pascalCase)
                .replace("{resource}", resourceName.toLowerCase());
    }

    private static String toPascalCase(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
    }
}
