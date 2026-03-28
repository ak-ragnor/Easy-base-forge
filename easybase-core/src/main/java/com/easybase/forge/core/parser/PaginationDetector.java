package com.easybase.forge.core.parser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;

/**
 * Determines whether an operation should be treated as paginated.
 *
 * <p>Detection strategy (in priority order):
 * <ol>
 *   <li>Explicit {@code x-easybase-paginated: true} extension on the operation.</li>
 *   <li>Presence of common pagination query parameters: {@code page} + {@code size} or {@code pageable}.</li>
 * </ol>
 */
public class PaginationDetector {

	private static final Set<String> PAGE_PARAMS = Set.of("page", "size", "pageable", "pageNumber", "pageSize");

	public boolean isPaginated(Operation operation) {
		if (operation == null) return false;

		Map<String, Object> extensions = operation.getExtensions();

		if (extensions != null) {
			Object flag = extensions.get("x-easybase-paginated");
			if (Boolean.TRUE.equals(flag) || "true".equals(flag)) {
				return true;
			}
		}

		List<Parameter> parameters = operation.getParameters();

		if (parameters == null) {
			return false;
		}

		return parameters.stream()
				.filter(p -> "query".equals(p.getIn()))
				.map(Parameter::getName)
				.anyMatch(PAGE_PARAMS::contains);
	}
}
