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
 *
 * <p>Per-resource DTO tracking: the {@link SchemaResolver} session is reset for each resource,
 * then all of that resource's endpoints are resolved within the session. This ensures that
 * composed types (oneOf/anyOf variants, etc.) are correctly attributed to the right resource.
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

        // Group raw (path, method, operation) triples by resource name — NO schema resolution yet.
        Map<String, List<OperationEntry>> byResource = new LinkedHashMap<>();
        openApi.getPaths().forEach((path, pathItem) ->
                pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                    String resourceName = deriveResourceName(path, operation);
                    byResource.computeIfAbsent(resourceName, k -> new ArrayList<>())
                              .add(new OperationEntry(path, httpMethod, operation));
                }));

        // Pre-register ALL component schemas so $ref resolution works across resources.
        // This ensures every schema is in the registry before per-resource processing starts.
        if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
            openApi.getComponents().getSchemas().forEach(schemaResolver::ensureDtoRegistered);
        }

        // Build each resource within its own session window.
        // Resolving endpoints INSIDE resetSession/getSessionDtos ensures that any DTOs
        // "discovered" during schema resolution (e.g. oneOf variants, inline objects) are
        // correctly attributed to the resource being processed.
        List<ApiResource> result = new ArrayList<>();
        byResource.forEach((rawName, opEntries) -> {
            schemaResolver.resetSession();

            List<ApiEndpoint> endpoints = opEntries.stream()
                    .map(e -> mapEndpoint(e.path(), e.httpMethod(), e.operation()))
                    .collect(Collectors.toList());

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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String deriveResourceName(String path, Operation operation) {
        if (operation.getTags() != null && !operation.getTags().isEmpty()) {
            return operation.getTags().get(0);
        }
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

    /** Lightweight holder for a raw operation before schema resolution. */
    private record OperationEntry(String path, PathItem.HttpMethod httpMethod, Operation operation) {}
}
