package com.easybase.forge.common.util;

import java.nio.file.Path;

/** Utility methods for converting package names to file-system paths. */
public final class PackageUtils {

	private PackageUtils() {
		throw new UnsupportedOperationException("Utility class");
	}

	/** Resolves a Java package name to a directory path relative to {@code base}. */
	public static Path packageToPath(Path base, String packageName) {
		return base.resolve(packageName.replace('.', '/'));
	}
}
