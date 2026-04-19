package com.easybase.forge.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

public abstract class ServiceExtension {

	public abstract RegularFileProperty getServiceConfigFile();

	public abstract RegularFileProperty getProjectConfigFile();

	public abstract DirectoryProperty getOutputDirectory();

	public abstract Property<Boolean> getSkip();
}
