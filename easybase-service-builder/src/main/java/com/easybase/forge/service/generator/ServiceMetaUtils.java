package com.easybase.forge.service.generator;

import java.util.List;

import com.easybase.forge.service.config.ServiceConfig;
import com.squareup.javapoet.TypeSpec;

public final class ServiceMetaUtils {

	private ServiceMetaUtils() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static void applyAuthors(TypeSpec.Builder builder, ServiceConfig config) {
		List<String> authors = config.getResolvedAuthors();

		if (authors == null || authors.isEmpty()) {
			return;
		}

		StringBuilder javadoc = new StringBuilder();

		for (String author : authors) {
			javadoc.append("\n@author ").append(author);
		}

		builder.addJavadoc(javadoc.toString());
	}
}
