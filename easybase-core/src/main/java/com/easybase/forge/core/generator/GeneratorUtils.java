package com.easybase.forge.core.generator;

import java.nio.file.Path;

import com.easybase.forge.core.config.GeneratorConfig;
import com.squareup.javapoet.TypeSpec;

public final class GeneratorUtils {

	private GeneratorUtils() {}

	/** Converts a Java package name to the corresponding directory path under {@code base}. */
	public static Path packageToPath(Path base, String packageName) {
		return base.resolve(packageName.replace('.', '/'));
	}

	/**
	 * Converts a kebab-case or dot-separated parameter name to lowerCamelCase.
	 *
	 * <p>Examples: {@code "pet-id"} → {@code "petId"}, {@code "pet.name"} → {@code "petName"}.
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
	 * Derives a request-body parameter name from a DTO class name by lowercasing the first letter.
	 *
	 * <p>Example: {@code "CreatePetRequest"} → {@code "createPetRequest"}.
	 */
	public static String deriveBodyParamName(String javaType) {
		if (javaType == null || javaType.isEmpty()) {
			return "body";
		}

		return Character.toLowerCase(javaType.charAt(0)) + javaType.substring(1);
	}

	/**
	 * Appends {@code @author} and {@code @generated} Javadoc tags to a type builder
	 * when {@code addGeneratedAnnotation} is enabled in the config.
	 */
	public static void addGeneratedJavadoc(TypeSpec.Builder builder, GeneratorConfig config) {
		if (!config.getGenerate().isAddGeneratedAnnotation()) {
			return;
		}

		StringBuilder doc = new StringBuilder();

		for (String author : config.getGenerate().getAllAuthors()) {
			doc.append("@author ").append(author).append("\n");
		}

		doc.append("@generated\n");
		builder.addJavadoc(doc.toString());
	}
}
