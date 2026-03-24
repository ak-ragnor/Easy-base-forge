package com.easybase.forge.core.generator.controller;

import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.config.PaginationMode;
import com.easybase.forge.core.config.ResponseEntityMode;
import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.generator.GeneratorUtils;
import com.easybase.forge.core.generator.TypeNameResolver;
import com.easybase.forge.core.model.*;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates the abstract {@code {Resource}ControllerBase} class for a resource.
 *
 * <p>This class is always overwritten on regeneration. It contains one method
 * per endpoint, each delegating to the injected {@code {Resource}ApiDelegate}.
 */
public class BaseControllerGenerator {

    private static final ClassName GET_MAPPING     = ClassName.get("org.springframework.web.bind.annotation", "GetMapping");
    private static final ClassName POST_MAPPING    = ClassName.get("org.springframework.web.bind.annotation", "PostMapping");
    private static final ClassName PUT_MAPPING     = ClassName.get("org.springframework.web.bind.annotation", "PutMapping");
    private static final ClassName PATCH_MAPPING   = ClassName.get("org.springframework.web.bind.annotation", "PatchMapping");
    private static final ClassName DELETE_MAPPING  = ClassName.get("org.springframework.web.bind.annotation", "DeleteMapping");
    private static final ClassName PATH_VARIABLE   = ClassName.get("org.springframework.web.bind.annotation", "PathVariable");
    private static final ClassName REQUEST_PARAM   = ClassName.get("org.springframework.web.bind.annotation", "RequestParam");
    private static final ClassName REQUEST_BODY    = ClassName.get("org.springframework.web.bind.annotation", "RequestBody");
    private static final ClassName VALID           = ClassName.get("jakarta.validation", "Valid");

    public GeneratedArtifact generate(ApiResource resource, GeneratorConfig config) {
        String basePkg = config.resolvePackage(
                config.getStructure().getController().getBasePkg(),
                resource.packageSuffix());
        String delegatePkg = config.resolvePackage(
                config.getStructure().getDelegate().getPkg(),
                resource.packageSuffix());
        String dtoPkg = config.resolvePackage(
                config.getStructure().getDto().getPkg(),
                resource.packageSuffix());
        TypeNameResolver typeResolver = new TypeNameResolver(dtoPkg);

        String delegateName = resource.name() + "ApiDelegate";
        String baseName = resource.name() + "ControllerBase";
        ClassName delegateType = ClassName.get(delegatePkg, delegateName);

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(baseName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addField(FieldSpec.builder(delegateType, "delegate", Modifier.PRIVATE, Modifier.FINAL).build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PROTECTED)
                        .addParameter(delegateType, "delegate")
                        .addStatement("this.delegate = delegate")
                        .build())
                .addMethod(MethodSpec.methodBuilder("getDelegate")
                        .addModifiers(Modifier.PROTECTED)
                        .returns(delegateType)
                        .addStatement("return delegate")
                        .build());

        addGeneratedJavadoc(classBuilder, config);

        for (ApiEndpoint endpoint : resource.endpoints()) {
            classBuilder.addMethod(buildEndpointMethod(endpoint, typeResolver, delegateType, config));
        }

        JavaFile javaFile = JavaFile.builder(basePkg, classBuilder.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();

        Path outputPath = GeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), basePkg)
                .resolve(baseName + ".java");

        return new GeneratedArtifact(outputPath, ArtifactType.BASE_CONTROLLER, javaFile.toString());
    }

    private MethodSpec buildEndpointMethod(ApiEndpoint endpoint, TypeNameResolver typeResolver,
                                            ClassName delegateType, GeneratorConfig config) {
        ResponseEntityMode mode = config.getGenerate().getResponseEntityWrapping();
        PaginationMode paginationMode = config.getGenerate().getPagination();
        boolean applyPagination = endpoint.paginated() && paginationMode == PaginationMode.SPRING_DATA;
        TypeName returnType = resolveReturnType(endpoint, typeResolver, mode, applyPagination);

        MethodSpec.Builder mb = MethodSpec.methodBuilder(endpoint.operationId())
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addAnnotation(mappingAnnotation(endpoint.httpMethod(), endpoint.path()));

        List<String> delegateArgs = new ArrayList<>();

        // Path + query parameters
        for (ApiParameter param : endpoint.parameters()) {
            if (param.in() == ParameterLocation.PATH || param.in() == ParameterLocation.QUERY) {
                String javaName = sanitizeName(param.name());
                ParameterSpec.Builder pb = ParameterSpec.builder(
                        typeResolver.resolve(param.schema().javaType()), javaName);

                if (param.in() == ParameterLocation.PATH) {
                    pb.addAnnotation(AnnotationSpec.builder(PATH_VARIABLE)
                            .addMember("value", "$S", param.name()).build());
                } else {
                    AnnotationSpec.Builder qa = AnnotationSpec.builder(REQUEST_PARAM)
                            .addMember("value", "$S", param.name());
                    if (!param.required()) qa.addMember("required", "false");
                    pb.addAnnotation(qa.build());
                }
                mb.addParameter(pb.build());
                delegateArgs.add(javaName);
            }
        }

        // Request body
        if (endpoint.requestBody() != null) {
            String bodyType = endpoint.requestBody().schema().javaType();
            String bodyName = deriveBodyParamName(bodyType);
            ParameterSpec.Builder pb = ParameterSpec.builder(typeResolver.resolve(bodyType), bodyName)
                    .addAnnotation(VALID)
                    .addAnnotation(AnnotationSpec.builder(REQUEST_BODY)
                            .addMember("required", "$L", endpoint.requestBody().required()).build());
            mb.addParameter(pb.build());
            delegateArgs.add(bodyName);
        }

        // Spring Data Pageable parameter (appended last)
        if (applyPagination) {
            mb.addParameter(TypeNameResolver.pageableType(), "pageable");
            delegateArgs.add("pageable");
        }

        String argList = String.join(", ", delegateArgs);
        String delegateCall = "delegate." + endpoint.operationId() + "(" + argList + ")";

        if (config.getGenerate().isAddGeneratedAnnotation()) {
            mb.addJavadoc("<pre>$L</pre>\n", buildCurlSnippet(endpoint));
        }

        boolean isVoid = returnType.equals(TypeName.VOID);

        if (isVoid) {
            mb.addStatement(delegateCall);
        } else {
            mb.addStatement("return $L", delegateCall);
        }

        return mb.build();
    }

    private static void addGeneratedJavadoc(TypeSpec.Builder classBuilder, GeneratorConfig config) {
        if (!config.getGenerate().isAddGeneratedAnnotation()) return;
        StringBuilder doc = new StringBuilder();
        String author = config.getGenerate().getAuthor();
        if (author != null && !author.isBlank()) {
            doc.append("@author ").append(author).append("\n");
        }
        doc.append("@generated\n");
        classBuilder.addJavadoc(doc.toString());
    }

    private static String buildCurlSnippet(ApiEndpoint endpoint) {
        StringBuilder sb = new StringBuilder("curl -X ")
                .append(endpoint.httpMethod().name())
                .append(" http://localhost:8080")
                .append(endpoint.path());
        if (endpoint.requestBody() != null) {
            sb.append(" \\\n  -H 'Content-Type: application/json' \\\n  -d '{}'");
        }
        return sb.toString();
    }

    private TypeName resolveReturnType(ApiEndpoint endpoint, TypeNameResolver typeResolver,
                                       ResponseEntityMode mode, boolean paginated) {
        ApiResponse primary = endpoint.primaryResponse();
        String bodyType = (primary != null && primary.schema() != null)
                ? primary.schema().javaType() : null;
        boolean isVoid = bodyType == null || bodyType.equals("Void");

        if (paginated && !isVoid) {
            return switch (mode) {
                case ALWAYS    -> typeResolver.responseEntityPage(bodyType);
                case NEVER     -> typeResolver.page(bodyType);
                case VOID_ONLY -> typeResolver.page(bodyType);
            };
        }

        return switch (mode) {
            case ALWAYS     -> typeResolver.responseEntity(bodyType);
            case NEVER      -> isVoid ? TypeName.VOID : typeResolver.resolve(bodyType);
            case VOID_ONLY  -> isVoid ? typeResolver.responseEntity(null) : typeResolver.resolve(bodyType);
        };
    }

    private static AnnotationSpec mappingAnnotation(HttpMethod method, String path) {
        ClassName annotation = switch (method) {
            case GET    -> GET_MAPPING;
            case POST   -> POST_MAPPING;
            case PUT    -> PUT_MAPPING;
            case PATCH  -> PATCH_MAPPING;
            case DELETE -> DELETE_MAPPING;
            default     -> GET_MAPPING;
        };
        return AnnotationSpec.builder(annotation).addMember("value", "$S", path).build();
    }

    private static String sanitizeName(String name) {
        String[] parts = name.split("[\\-\\.]");
        if (parts.length == 1) return name;
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
                sb.append(parts[i].substring(1));
            }
        }
        return sb.toString();
    }

    private static String deriveBodyParamName(String javaType) {
        if (javaType == null || javaType.isEmpty()) return "body";
        return Character.toLowerCase(javaType.charAt(0)) + javaType.substring(1);
    }
}
