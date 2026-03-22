package com.easybase.forge.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and validates {@link GeneratorConfig} from a YAML file.
 */
public class ConfigLoader {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory())
            .findAndRegisterModules();

    public static GeneratorConfig load(Path configFile) {
        if (!Files.exists(configFile)) {
            throw new ConfigException("Config file not found: " + configFile.toAbsolutePath());
        }

        GeneratorConfig config;
        try (InputStream in = Files.newInputStream(configFile)) {
            config = YAML_MAPPER.readValue(in, GeneratorConfig.class);
        } catch (IOException e) {
            throw new ConfigException("Failed to parse config file: " + configFile, e);
        }

        validate(config, configFile);

        if (config.getResolvedOutputDirectory() == null && config.getOutput().getDirectory() != null) {
            Path outputDir = configFile.getParent().resolve(config.getOutput().getDirectory()).normalize();
            config.withOutputDirectory(outputDir);
        }

        return config;
    }

    private static void validate(GeneratorConfig config, Path configFile) {
        if (config.getBasePackage() == null || config.getBasePackage().isBlank()) {
            throw new ConfigException("'basePackage' is required in " + configFile);
        }
        if (config.getOutput().getDirectory() == null
                && config.getResolvedOutputDirectory() == null) {
            throw new ConfigException("'output.directory' is required in " + configFile);
        }
    }
}
