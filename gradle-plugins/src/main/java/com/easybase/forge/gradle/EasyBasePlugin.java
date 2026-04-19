package com.easybase.forge.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class EasyBasePlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPlugins().apply(RestPlugin.class);
		project.getPlugins().apply(ServicePlugin.class);
	}
}
