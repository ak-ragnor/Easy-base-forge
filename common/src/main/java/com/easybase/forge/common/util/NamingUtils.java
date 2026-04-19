package com.easybase.forge.common.util;

/**
 * Centralised naming and case-conversion utilities shared by both builders.
 *
 * <p>All methods are pure functions with no side effects.
 */
public final class NamingUtils {

	private NamingUtils() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Converts a kebab-case or dot-notation name to lowerCamelCase.
	 * <p>Examples: {@code "my-name"} → {@code "myName"}, {@code "foo.bar"} → {@code "fooBar"}.
	 * Single-segment names are returned unchanged.
	 */
	public static String sanitizeName(String name) {
		String[] parts = name.split("[\\-\\.]");

		if (parts.length == 1) {
			return name;
		}

		StringBuilder sb = new StringBuilder(parts[0]);

		for (int i = 1; i < parts.length; i++) {
			if (!parts[i].isEmpty()) {
				sb.append(Character.toUpperCase(parts[i].charAt(0)));
				sb.append(parts[i].substring(1));
			}
		}

		return sb.toString();
	}

	/**
	 * Derives a method parameter name from a DTO class name.
	 * <p>Example: {@code "PetDTO"} → {@code "petDTO"}.
	 */
	public static String deriveBodyParamName(String javaType) {
		if (javaType == null || javaType.isEmpty()) {
			return "body";
		}

		return Character.toLowerCase(javaType.charAt(0)) + javaType.substring(1);
	}

	/**
	 * Converts a camelCase or PascalCase name to snake_case.
	 * <p>Example: {@code "UserEntity"} → {@code "user_entity"}.
	 */
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

	/**
	 * Converts a snake_case string to lowerCamelCase.
	 * <p>Example: {@code "created_at"} → {@code "createdAt"}.
	 */
	public static String snakeToCamelCase(String snake) {
		StringBuilder result = new StringBuilder();
		boolean nextUpper = false;

		for (char c : snake.toCharArray()) {
			if (c == '_') {
				nextUpper = true;
			} else if (nextUpper) {
				result.append(Character.toUpperCase(c));
				nextUpper = false;
			} else {
				result.append(c);
			}
		}

		return result.toString();
	}

	/**
	 * Converts a string to PascalCase, treating hyphens and underscores as word boundaries.
	 * <p>Example: {@code "pet-store"} → {@code "PetStore"}.
	 */
	public static String toPascalCase(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}

		String[] parts = name.split("[_\\-]");
		StringBuilder sb = new StringBuilder();

		for (String part : parts) {
			if (!part.isEmpty()) {
				sb.append(Character.toUpperCase(part.charAt(0)));

				if (part.length() > 1) {
					sb.append(part.substring(1));
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Converts a string to lowerCamelCase, treating hyphens and underscores as word boundaries.
	 * <p>Example: {@code "pet-store"} → {@code "petStore"}.
	 */
	public static String toLowerCamelCase(String name) {
		String pascal = toPascalCase(name);

		if (pascal == null || pascal.isEmpty()) {
			return pascal;
		}

		return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
	}
}
