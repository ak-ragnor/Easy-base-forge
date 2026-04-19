package com.easybase.forge.gradle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public class ServicePlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		ServiceExtension ext = project.getExtensions().create("easybaseService", ServiceExtension.class);

		ext.getProjectConfigFile()
				.convention(project.getLayout().getProjectDirectory().file("easybase-config.yaml"));
		ext.getSkip().convention(false);

		TaskProvider<GenerateServiceTask> generateTask = project.getTasks()
				.register("easybaseGenerateService", GenerateServiceTask.class, task -> {
					task.setGroup("easybase");
					task.setDescription(
							"Generates Spring service layer (model, repository, hooks, service) from a service.yml spec.");

					task.getServiceConfigFile().set(ext.getServiceConfigFile());
					task.getProjectConfigFile().set(ext.getProjectConfigFile());

					task.getOutputDirectoryOverride()
							.set(ext.getOutputDirectory().map(d -> d.getAsFile().getAbsolutePath()));

					task.onlyIf("skip is false", t -> !ext.getSkip().get());
				});

		project.getPluginManager().withPlugin("java", appliedPlugin -> {
			project.getTasks().named("compileJava").configure(compileJava -> compileJava.dependsOn(generateTask));

			injectServiceBuilderDependency(project);
		});
	}

	private void injectServiceBuilderDependency(Project project) {
		String version = readVersion();
		project.getDependencies().add("implementation", "com.easybase:easybase-service-builder:" + version);
	}

	private String readVersion() {
		try (InputStream in = ServicePlugin.class.getResourceAsStream("/easybase-version.properties")) {
			if (in == null) {
				return "0.1.0-SNAPSHOT";
			}
			Properties props = new Properties();
			props.load(in);
			return props.getProperty("version", "0.1.0-SNAPSHOT");
		} catch (IOException e) {
			return "0.1.0-SNAPSHOT";
		}
	}
}
