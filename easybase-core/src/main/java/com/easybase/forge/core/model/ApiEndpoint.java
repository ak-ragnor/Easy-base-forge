package com.easybase.forge.core.model;

import java.util.List;
import java.util.Map;

public record ApiEndpoint(
		String operationId,
		HttpMethod httpMethod,
		String path,
		String summary,
		List<ApiParameter> parameters,
		ApiRequestBody requestBody,
		Map<Integer, ApiResponse> responses,
		boolean paginated,
		List<String> tags) {
	public ApiResponse primaryResponse() {
		return responses.entrySet().stream()
				.filter(e -> e.getKey() >= 200 && e.getKey() < 300)
				.findFirst()
				.map(Map.Entry::getValue)
				.orElse(null);
	}
}
