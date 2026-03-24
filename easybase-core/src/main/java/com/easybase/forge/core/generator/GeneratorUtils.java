package com.easybase.forge.core.generator;

import java.nio.file.Path;

/** Shared utilities for all artifact generators. */
public final class GeneratorUtils {

	private GeneratorUtils() {}

	/** Converts a Java package name to the corresponding directory path under {@code base}. */
	public static Path packageToPath(Path base, String packageName) {
		return base.resolve(packageName.replace('.', '/'));
	}
}
