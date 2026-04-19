package com.easybase.forge.service.generator;

import com.easybase.forge.service.config.ServiceConfig;

public final class ServiceTableUtils {

	private ServiceTableUtils() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static String tableName(ServiceConfig config) {
		String snake = toSnakeCase(config.getEntity());
		String prefix = config.getTablePrefix();

		if (prefix == null || prefix.isBlank()) {
			return snake + "s";
		}

		return prefix + snake + "s";
	}

	public static String toSnakeCase(String name) {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);

			if (Character.isUpperCase(c) && i > 0) {
				result.append('_');
			}

			result.append(Character.toLowerCase(c));
		}

		return result.toString();
	}

	public static String deriveForeignKeyColumn(String fieldName) {
		return toSnakeCase(fieldName) + "_id";
	}

	public static String deriveForeignKeyName(String tableName, String columnName) {
		return "fk_" + tableName + "_" + columnName;
	}
}
