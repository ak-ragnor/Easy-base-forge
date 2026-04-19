package com.easybase.forge.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public class RestPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		EasyBaseExtension ext = project.getExtensions().create("easybase", EasyBaseExtension.class);

		ext.getConfigFile().convention(project.getLayout().getProjectDirectory().file("easybase-config.yaml"));
		ext.getSkip().convention(false);

		TaskProvider<GenerateRestTask> generateTask = project.getTasks()
				.register("easybaseGenerateRest", GenerateRestTask.class, task -> {
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
