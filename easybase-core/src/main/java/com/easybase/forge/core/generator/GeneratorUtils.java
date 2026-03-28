package com.easybase.forge.core.generator;

import java.nio.file.Path;

import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.config.PaginationMode;
import com.easybase.forge.core.model.ApiEndpoint;
import com.easybase.forge.core.model.ApiParameter;
import com.easybase.forge.core.model.ParameterLocation;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public final class GeneratorUtils {

	private GeneratorUtils() {}

	public static Path packageToPath(Path base, String packageName) {
		return base.resolve(packageName.replace('.', '/'));
	}

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

	public static String deriveBodyParamName(String javaType) {
		if (javaType == null || javaType.isEmpty()) {
			return "body";
		}

		return Character.toLowerCase(javaType.charAt(0)) + javaType.substring(1);
	}

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

	public static void addEndpointParameters(
			MethodSpec.Builder methodBuilder,
			ApiEndpoint endpoint,
			TypeNameResolver typeResolver,
			GeneratorConfig config) {

		for (ApiParameter param : endpoint.parameters()) {
			if (param.in() == ParameterLocation.PATH || param.in() == ParameterLocation.QUERY) {
				methodBuilder.addParameter(typeResolver.resolve(param.schema().javaType()), sanitizeName(param.name()));
			}
		}

		if (endpoint.requestBody() != null) {
			String bodyType = endpoint.requestBody().schema().javaType();
			methodBuilder.addParameter(typeResolver.resolve(bodyType), deriveBodyParamName(bodyType));
		}

		if (endpoint.paginated() && config.getGenerate().getPagination() == PaginationMode.SPRING_DATA) {
			methodBuilder.addParameter(TypeNameResolver.pageableType(), "pageable");
		}
	}
}
