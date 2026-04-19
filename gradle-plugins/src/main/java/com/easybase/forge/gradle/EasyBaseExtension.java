package com.easybase.forge.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

public abstract class EasyBaseExtension {

	public abstract RegularFileProperty getSpecFile();

	public abstract RegularFileProperty getConfigFile();

	public abstract DirectoryProperty getOutputDirectory();

	public abstract Property<Boolean> getSkip();
}
