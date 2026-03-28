package com.easybase.forge.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

/**
 * Gradle plugin that generates Spring Boot REST layers from OpenAPI specifications.
 *
 * <p>Registers the {@code easybaseGenerate} task and wires it as a dependency of {@code compileJava}.
 *
 * <p>Apply in {@code build.gradle}:
 * <pre>{@code
 * plugins {
 *     id 'com.easybase.forge' version '0.1.0-SNAPSHOT'
 * }
 *
 * easybase {
 *     specFile = file('src/main/resources/api.yaml')
 * }
 * }</pre>
 */
public class EasyBasePlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		EasyBaseExtension ext = project.getExtensions().create("easybase", EasyBaseExtension.class);

		ext.getConfigFile().convention(project.getLayout().getProjectDirectory().file("easybase-config.yaml"));
		ext.getSkip().convention(false);

		TaskProvider<GenerateTask> generateTask = project.getTasks()
				.register("easybaseGenerate", GenerateTask.class, task -> {
					task.setGroup("easybase");
					task.setDescription(
							"Generates Spring REST layer (controllers, delegates, DTOs) from an OpenAPI spec.");

					task.getSpecFile().set(ext.getSpecFile());
					task.getConfigFile().set(ext.getConfigFile());

					task.getOutputDirectoryOverride()
							.set(ext.getOutputDirectory().map(d -> d.getAsFile().getAbsolutePath()));

					task.onlyIf("skip is false", t -> !ext.getSkip().get());
				});

		project.getPluginManager().withPlugin("java", appliedPlugin -> project.getTasks()
				.named("compileJava")
				.configure(compileJava -> compileJava.dependsOn(generateTask)));
	}
}
