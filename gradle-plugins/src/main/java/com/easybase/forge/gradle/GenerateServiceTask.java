package com.easybase.forge.gradle;

import java.nio.file.Path;
import java.util.List;

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

import com.easybase.forge.common.config.ConfigException;
import com.easybase.forge.common.io.GenerationReport;
import com.easybase.forge.service.config.ServiceConfig;
import com.easybase.forge.service.config.ServiceConfigLoader;
import com.easybase.forge.service.engine.ServiceEngine;

@CacheableTask
public abstract class GenerateServiceTask extends DefaultTask {

	@InputFile
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract RegularFileProperty getServiceConfigFile();

	@Optional
	@InputFile
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract RegularFileProperty getProjectConfigFile();

	@Optional
	@Input
	public abstract Property<String> getOutputDirectoryOverride();

	@TaskAction
	public void generate() {
		Logger log = getLogger();

		Path serviceConfigPath = getServiceConfigFile().get().getAsFile().toPath();

		log.lifecycle("EasyBase Service Builder: service config {}", serviceConfigPath);

		Path projectConfigPath = null;

		if (getProjectConfigFile().isPresent()) {
			Path p = getProjectConfigFile().get().getAsFile().toPath();
			if (p.toFile().exists()) {
				projectConfigPath = p;
			}
		}

		log.lifecycle("EasyBase Service Builder: project config {}", projectConfigPath);

		Path outputOverride = getOutputDirectoryOverride().isPresent()
				? Path.of(getOutputDirectoryOverride().get())
				: null;

		List<ServiceConfig> configs;

		try {
			configs = ServiceConfigLoader.loadAll(projectConfigPath, serviceConfigPath, outputOverride);
		} catch (ConfigException e) {
			throw new GradleException("Failed to load EasyBase service config: " + e.getMessage(), e);
		}

		log.lifecycle("EasyBase Service Builder: output {}", configs.get(0).getResolvedOutputDirectory());

		for (ServiceConfig config : configs) {
			log.lifecycle(
					"EasyBase Service Builder: generating entity {} (module: {})",
					config.getEntity(),
					config.getModuleName());

			GenerationReport report;

			try {
				report = new ServiceEngine(config).generate();
			} catch (Exception e) {
				throw new GradleException(
						"EasyBase service generation failed for entity " + config.getEntity() + ": " + e.getMessage(),
						e);
			}

			report.created().forEach(p -> log.lifecycle("[CREATED] {}", p));
			report.updated().forEach(p -> log.lifecycle("[UPDATED] {}", p));
			report.skipped().forEach(p -> log.lifecycle("[SKIPPED] {}", p));
			report.errors().forEach(e -> log.error("[ERROR]   {}", e));

			if (report.hasErrors()) {
				throw new GradleException("EasyBase service generation completed with errors for entity "
						+ config.getEntity() + ": " + report.errorSummary());
			}
		}
	}
}
