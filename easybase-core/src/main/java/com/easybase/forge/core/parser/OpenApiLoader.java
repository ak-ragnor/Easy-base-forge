package com.easybase.forge.core.parser;

import java.nio.file.Path;
import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

/**
 * Loads and validates an OpenAPI 3.x document.
 * Returns a fully-resolved {@link OpenAPI} model with all {@code $ref} chains followed.
 */
public class OpenApiLoader {

	public OpenAPI load(Path specFile) {
		ParseOptions options = new ParseOptions();

		options.setResolve(true);

		SwaggerParseResult result =
				new OpenAPIV3Parser().readLocation(specFile.toAbsolutePath().toString(), null, options);

		List<String> messages = result.getMessages();

		if (result.getOpenAPI() == null) {
			String errors = messages == null ? "(no details)" : String.join(", ", messages);
			throw new ParseException("Failed to parse OpenAPI spec at " + specFile + ": " + errors);
		}

		if (messages != null && !messages.isEmpty()) {
			messages.forEach(msg -> System.err.println("[WARN] OpenAPI: " + msg));
		}

		return result.getOpenAPI();
	}
}
