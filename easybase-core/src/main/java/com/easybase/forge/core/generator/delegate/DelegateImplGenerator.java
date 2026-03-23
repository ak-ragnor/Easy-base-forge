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
 * Generates the {@code {Resource}ApiDelegateImpl} stub class for a resource.
 *
 * <p>Only produced when {@code generate.delegateImpl: true} is set in the config.
 * This file is created once and never overwritten — users fill in the business logic.
 * Each method throws {@link UnsupportedOperationException} until implemented.
 */
public class DelegateImplGenerator {

    private static final ClassName COMPONENT =
            ClassName.get("org.springframework.stereotype", "Component");

    public GeneratedArtifact generate(ApiResource resource, GeneratorConfig config) {
        String delegatePkg = config.resolvePackage(
                config.getStructure().getDelegate().getPkg(),
                resource.packageSuffix());
        String dtoPkg = config.resolvePackage(
                config.getStructure().getDto().getPkg(),
                resource.packageSuffix());
        TypeNameResolver typeResolver = new TypeNameResolver(dtoPkg);

        String delegateName = resource.name() + "ApiDelegate";
        String implName = resource.name() + "ApiDelegateImpl";
        ClassName delegateType = ClassName.get(delegatePkg, delegateName);

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(implName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(COMPONENT)
                .addSuperinterface(delegateType);

        for (ApiEndpoint endpoint : resource.endpoints()) {
            classBuilder.addMethod(buildStubMethod(endpoint, typeResolver, config));
        }

        JavaFile javaFile = JavaFile.builder(delegatePkg, classBuilder.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();

        Path outputPath = GeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), delegatePkg)
                .resolve(implName + ".java");

        return new GeneratedArtifact(outputPath, ArtifactType.DELEGATE_IMPL, javaFile.toString());
    }

    private MethodSpec buildStubMethod(ApiEndpoint endpoint, TypeNameResolver typeResolver,
                                       GeneratorConfig config) {
        ResponseEntityMode mode = config.getGenerate().getResponseEntityWrapping();
        PaginationMode paginationMode = config.getGenerate().getPagination();
        boolean applyPagination = endpoint.paginated() && paginationMode == PaginationMode.SPRING_DATA;
        TypeName returnType = resolveReturnType(endpoint, typeResolver, mode, applyPagination);

        MethodSpec.Builder mb = MethodSpec.methodBuilder(endpoint.operationId())
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType);

        // Path + query parameters
        for (ApiParameter param : endpoint.parameters()) {
            if (param.in() == ParameterLocation.PATH || param.in() == ParameterLocation.QUERY) {
                mb.addParameter(
                        typeResolver.resolve(param.schema().javaType()),
                        sanitizeName(param.name()));
            }
        }

        // Request body
        if (endpoint.requestBody() != null) {
            String bodyType = endpoint.requestBody().schema().javaType();
            mb.addParameter(typeResolver.resolve(bodyType), deriveBodyParamName(bodyType));
        }

        // Spring Data pageable parameter
        if (applyPagination) {
            mb.addParameter(TypeNameResolver.pageableType(), "pageable");
        }

        mb.addStatement("throw new $T($S)", UnsupportedOperationException.class, "Not implemented");

        return mb.build();
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
