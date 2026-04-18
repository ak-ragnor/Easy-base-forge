package com.easybase.forge.core.service.config;

public final class ServiceLayoutResolver {

	public static final String MULTI_MODULE = "MULTI_MODULE";
	public static final String FLAT = "FLAT";

	private ServiceLayoutResolver() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static String resolve(String pattern, String basePackage, String moduleName, String layout) {
		String result = pattern.replace("{basePackage}", basePackage != null ? basePackage : "");

		if (FLAT.equalsIgnoreCase(layout)) {
			result = result.replace(".{module}", "").replace("{module}.", "").replace("{module}", "");
		} else {
			String module = moduleName != null ? moduleName.toLowerCase() : "";
			result = result.replace("{module}", module);
		}

		while (result.contains("..")) {
			result = result.replace("..", ".");
		}

		if (result.endsWith(".")) {
			result = result.substring(0, result.length() - 1);
		}

		return result;
	}
}
