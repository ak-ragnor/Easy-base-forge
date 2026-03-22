package com.easybase.forge.core.generator.delegate;

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
 * Generates the {@code {Resource}ApiDelegate} interface for a resource.
 *
 * <p>One method per endpoint, return types governed by {@link ResponseEntityMode}.
 */
public class DelegateGenerator {

    public GeneratedArtifact generate(ApiResource resource, GeneratorConfig config) {
        String delegatePkg = config.resolvePackage(
                config.getStructure().getDelegate().getPkg(),
                resource.packageSuffix());
        String dtoPkg = config.resolvePackage(
                config.getStructure().getDto().getPkg(),
                resource.packageSuffix());
        TypeNameResolver typeResolver = new TypeNameResolver(dtoPkg);

        String delegateName = resource.name() + "ApiDelegate";

        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(delegateName)
                .addModifiers(Modifier.PUBLIC);

        for (ApiEndpoint endpoint : resource.endpoints()) {
            MethodSpec method = buildMethod(endpoint, typeResolver,
                    config.getGenerate().getResponseEntityWrapping(),
                    config.getGenerate().getPagination());
            interfaceBuilder.addMethod(method);
        }

        JavaFile javaFile = JavaFile.builder(delegatePkg, interfaceBuilder.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();

        Path outputPath = GeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), delegatePkg)
                .resolve(delegateName + ".java");

        return new GeneratedArtifact(outputPath, ArtifactType.DELEGATE, javaFile.toString());
    }

    private MethodSpec buildMethod(ApiEndpoint endpoint, TypeNameResolver typeResolver,
                                   ResponseEntityMode mode, PaginationMode paginationMode) {
        boolean applyPagination = endpoint.paginated() && paginationMode == PaginationMode.SPRING_DATA;
        TypeName returnType = resolveReturnType(endpoint, typeResolver, mode, applyPagination);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(endpoint.operationId())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(returnType);

        // Path + query parameters
        for (ApiParameter param : endpoint.parameters()) {
            if (param.in() == ParameterLocation.PATH || param.in() == ParameterLocation.QUERY) {
                methodBuilder.addParameter(
                        typeResolver.resolve(param.schema().javaType()),
                        sanitizeName(param.name()));
            }
        }

        // Request body
        if (endpoint.requestBody() != null) {
            String bodyType = endpoint.requestBody().schema().javaType();
            methodBuilder.addParameter(typeResolver.resolve(bodyType), deriveBodyParamName(bodyType));
        }

        // Spring Data pageable parameter
        if (applyPagination) {
            methodBuilder.addParameter(TypeNameResolver.pageableType(), "pageable");
        }

        return methodBuilder.build();
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
            case ALWAYS    -> typeResolver.responseEntity(bodyType);
            case NEVER     -> isVoid ? TypeName.VOID : typeResolver.resolve(bodyType);
            case VOID_ONLY -> isVoid ? typeResolver.responseEntity(null) : typeResolver.resolve(bodyType);
        };
    }

    private static String sanitizeName(String name) {
        // Convert kebab-case or dots to camelCase for Java identifiers
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
