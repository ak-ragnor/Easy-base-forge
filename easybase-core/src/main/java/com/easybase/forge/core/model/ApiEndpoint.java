package com.easybase.forge.core.model;

import java.util.List;
import java.util.Map;

public record ApiEndpoint(
        String operationId,
        HttpMethod httpMethod,
        String path,
        String summary,
        List<ApiParameter> parameters,
        /** Null if the operation has no request body. */
        ApiRequestBody requestBody,
        /** Keyed by HTTP status code. */
        Map<Integer, ApiResponse> responses,
        /** True when operation is tagged with x-easybase-paginated or has page/size query params. */
        boolean paginated,
        List<String> tags
) {
    /** Returns the primary success response (2xx), or null if none. */
    public ApiResponse primaryResponse() {
        return responses.entrySet().stream()
                .filter(e -> e.getKey() >= 200 && e.getKey() < 300)
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }
}
