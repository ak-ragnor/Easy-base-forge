package com.easybase.forge.core.parser;

import com.easybase.forge.core.model.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Extracts {@link ApiResource} objects from a resolved {@link OpenAPI} document.
 *
 * <p>Grouping strategy: operations are grouped by their first tag. If an operation
 * has no tags, the first path segment is used as the group name.
 */
public class ResourceExtractor {

    private final SchemaResolver schemaResolver;
    private final ValidationMapper validationMapper;
    private final PaginationDetector paginationDetector;

    public ResourceExtractor(SchemaResolver schemaResolver,
                             ValidationMapper validationMapper,
                             PaginationDetector paginationDetector) {
        this.schemaResolver = schemaResolver;
        this.validationMapper = validationMapper;
        this.paginationDetector = paginationDetector;
    }

    public List<ApiResource> extract(OpenAPI openApi) {
        if (openApi.getPaths() == null) return List.of();

        // Group (path, method, operation) triples by resource name
        Map<String, List<ApiEndpoint>> byResource = new LinkedHashMap<>();

        openApi.getPaths().forEach((path, pathItem) -> {
            pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                String resourceName = deriveResourceName(path, operation);
                ApiEndpoint endpoint = mapEndpoint(path, httpMethod, operation);
                byResource.computeIfAbsent(resourceName, k -> new ArrayList<>()).add(endpoint);
            });
        });

        // Pre-register all component schemas so $ref resolution works across resources.
        // Reset the session afterwards so pre-registration doesn't pollute per-resource tracking.
        if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
            openApi.getComponents().getSchemas().forEach(schemaResolver::ensureDtoRegistered);
        }

        // Build ApiResource list — process endpoint schemas per resource to track their DTOs
        List<ApiResource> result = new ArrayList<>();
        byResource.forEach((rawName, endpoints) -> {
            schemaResolver.resetSession();
            trackEndpointSchemas(endpoints);
            List<DtoSchema> dtos = schemaResolver.getSessionDtos();
            String name = SchemaResolver.toPascalCase(rawName);
            String suffix = rawName.toLowerCase();
            result.add(new ApiResource(name, suffix, endpoints, dtos));
        });
        return result;
    }

    private ApiEndpoint mapEndpoint(String path, PathItem.HttpMethod httpMethod, Operation operation) {
        List<ApiParameter> parameters = mapParameters(operation.getParameters());
        ApiRequestBody requestBody = mapRequestBody(operation);
        Map<Integer, ApiResponse> responses = mapResponses(operation.getResponses(), operation.getOperationId());
        boolean paginated = paginationDetector.isPaginated(operation);
        List<String> tags = operation.getTags() != null ? operation.getTags() : List.of();
        String operationId = normalizeOperationId(operation.getOperationId(), httpMethod, path);

        return new ApiEndpoint(
                operationId,
                HttpMethod.valueOf(httpMethod.name()),
                path,
                operation.getSummary(),
                parameters,
                requestBody,
                responses,
                paginated,
                tags
        );
    }

    private List<ApiParameter> mapParameters(List<Parameter> params) {
        if (params == null) return List.of();
        return params.stream()
                .map(p -> {
                    ApiSchema schema = schemaResolver.resolve(p.getSchema(), SchemaResolver.toPascalCase(p.getName()));
                    List<ValidationConstraint> constraints = validationMapper.map(p.getSchema(), Boolean.TRUE.equals(p.getRequired()));
                    ParameterLocation loc = ParameterLocation.valueOf(p.getIn().toUpperCase());
                    return new ApiParameter(p.getName(), loc, Boolean.TRUE.equals(p.getRequired()), schema, constraints);
                })
                .collect(Collectors.toList());
    }

    private ApiRequestBody mapRequestBody(Operation operation) {
        if (operation.getRequestBody() == null) return null;
        io.swagger.v3.oas.models.parameters.RequestBody rb = operation.getRequestBody();
        Content content = rb.getContent();
        if (content == null || content.isEmpty()) return null;

        Map.Entry<String, MediaType> first = content.entrySet().iterator().next();
        String contentType = first.getKey();
        ApiSchema schema = first.getValue().getSchema() != null
                ? schemaResolver.resolve(first.getValue().getSchema(), deriveRequestBodyHint(operation))
                : ApiSchema.voidSchema();

        return new ApiRequestBody(Boolean.TRUE.equals(rb.getRequired()), contentType, schema);
    }

    private Map<Integer, ApiResponse> mapResponses(ApiResponses apiResponses, String operationId) {
        if (apiResponses == null) return Map.of();
        Map<Integer, ApiResponse> result = new LinkedHashMap<>();
        apiResponses.forEach((code, response) -> {
            int statusCode;
            try {
                statusCode = Integer.parseInt(code);
            } catch (NumberFormatException e) {
                return; // skip "default"
            }
            ApiSchema schema = null;
            Content content = response.getContent();
            if (content != null && !content.isEmpty()) {
                MediaType mediaType = content.entrySet().iterator().next().getValue();
                if (mediaType.getSchema() != null) {
                    schema = schemaResolver.resolve(mediaType.getSchema(), operationId + "Response");
                }
            }
            result.put(statusCode, new ApiResponse(statusCode, response.getDescription(), schema));
        });
        return result;
    }

    /**
     * Walks already-built endpoint type names and tells the SchemaResolver to mark
     * each one as referenced in the current session.
     */
    private void trackEndpointSchemas(List<ApiEndpoint> endpoints) {
        for (ApiEndpoint endpoint : endpoints) {
            for (ApiParameter param : endpoint.parameters()) {
                if (param.schema() != null) {
                    trackTypeName(param.schema().javaType());
                }
            }
            if (endpoint.requestBody() != null && endpoint.requestBody().schema() != null) {
                trackTypeName(endpoint.requestBody().schema().javaType());
            }
            for (ApiResponse response : endpoint.responses().values()) {
                if (response.schema() != null) {
                    trackTypeName(response.schema().javaType());
                }
            }
        }
    }

    /** Tracks a resolved Java type name (unwraps List&lt;X&gt; → X first). */
    private void trackTypeName(String javaType) {
        if (javaType == null || javaType.isBlank()) return;
        if (javaType.startsWith("List<") && javaType.endsWith(">")) {
            trackTypeName(javaType.substring(5, javaType.length() - 1));
        } else {
            schemaResolver.ensureSessionTracked(javaType);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String deriveResourceName(String path, Operation operation) {
        if (operation.getTags() != null && !operation.getTags().isEmpty()) {
            return operation.getTags().get(0);
        }
        // First non-empty path segment, e.g. /pets/{id} → pets
        String[] segments = path.split("/");
        for (String seg : segments) {
            if (!seg.isEmpty() && !seg.startsWith("{")) {
                return seg;
            }
        }
        return "default";
    }

    private static String normalizeOperationId(String operationId, PathItem.HttpMethod method, String path) {
        if (operationId != null && !operationId.isBlank()) {
            return SchemaResolver.toLowerCamelCase(operationId);
        }
        // Derive from method + path: GET /pets/{id} → getPetsById
        String pathPart = path.replaceAll("\\{([^}]+)}", "By$1")
                .replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        return method.name().toLowerCase() + SchemaResolver.toPascalCase(pathPart);
    }

    private static String deriveRequestBodyHint(Operation operation) {
        if (operation.getOperationId() != null) {
            return SchemaResolver.toPascalCase(operation.getOperationId()) + "Request";
        }
        return "Request";
    }
}
