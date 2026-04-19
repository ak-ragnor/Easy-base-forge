package com.easybase.forge.rest.generator;

import com.easybase.forge.common.config.GeneratorConfig;
import com.easybase.forge.common.config.PaginationMode;
import com.easybase.forge.common.util.NamingUtils;
import com.easybase.forge.rest.model.ApiEndpoint;
import com.easybase.forge.rest.model.ApiParameter;
import com.easybase.forge.rest.model.ParameterLocation;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public final class RestGeneratorUtils {

	private RestGeneratorUtils() {
		throw new UnsupportedOperationException("Utility class");
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
				methodBuilder.addParameter(
						typeResolver.resolve(param.schema().javaType()), NamingUtils.sanitizeName(param.name()));
			}
		}

		if (endpoint.requestBody() != null) {
			String bodyType = endpoint.requestBody().schema().javaType();
			methodBuilder.addParameter(typeResolver.resolve(bodyType), NamingUtils.deriveBodyParamName(bodyType));
		}

		if (endpoint.paginated() && config.getGenerate().getPagination() == PaginationMode.SPRING_DATA) {
			methodBuilder.addParameter(TypeNameResolver.pageableType(), "pageable");
		}
	}
}
