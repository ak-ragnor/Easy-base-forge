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
 * Generates the delegate implementation pair for a resource:
 *
 * <ol>
 *   <li>{@code {Resource}ApiDelegateImplBase} in {@code delegate.impl.base} — abstract class
 *       implementing the delegate interface with stub methods. Always overwritten on regeneration,
 *       so new endpoints automatically appear here.</li>
 *   <li>{@code {Resource}ApiDelegateImpl} in {@code delegate.impl} — concrete {@code @Component}
 *       class extending the base. Created once and never overwritten; users place business
 *       logic here by overriding only the methods they need.</li>
 * </ol>
 *
 * <p>Only produced when {@code generate.delegateImpl: true} is set in the config.
 */
public class DelegateImplGenerator {

    private static final ClassName COMPONENT =
            ClassName.get("org.springframework.stereotype", "Component");

    public List<GeneratedArtifact> generate(ApiResource resource, GeneratorConfig config) {
        String delegatePkg = config.resolvePackage(
                config.getStructure().getDelegate().getPkg(),
                resource.packageSuffix());
        String dtoPkg = config.resolvePackage(
                config.getStructure().getDto().getPkg(),
                resource.packageSuffix());
        TypeNameResolver typeResolver = new TypeNameResolver(dtoPkg);

        String implPkg = delegatePkg + ".impl";
        String basePkg  = implPkg + ".base";

        String delegateName = resource.name() + "ApiDelegate";
        String baseName     = resource.name() + "ApiDelegateImplBase";
        String implName     = resource.name() + "ApiDelegateImpl";

        ClassName delegateType = ClassName.get(delegatePkg, delegateName);
        ClassName baseType     = ClassName.get(basePkg, baseName);

        // ── Base: abstract class with all stubs (always overwritten) ─────────
        TypeSpec.Builder baseBuilder = TypeSpec.classBuilder(baseName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addSuperinterface(delegateType);

        for (ApiEndpoint endpoint : resource.endpoints()) {
            baseBuilder.addMethod(buildStubMethod(endpoint, typeResolver, config));
        }

        JavaFile baseFile = JavaFile.builder(basePkg, baseBuilder.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();

        Path basePath = GeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), basePkg)
                .resolve(baseName + ".java");

        // ── Impl: empty @Component class extending the base (create-once) ────
        TypeSpec.Builder implBuilder = TypeSpec.classBuilder(implName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(COMPONENT)
                .superclass(baseType);

        JavaFile implFile = JavaFile.builder(implPkg, implBuilder.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();

        Path implPath = GeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), implPkg)
                .resolve(implName + ".java");

        List<GeneratedArtifact> artifacts = new ArrayList<>();
        artifacts.add(new GeneratedArtifact(basePath, ArtifactType.DELEGATE_IMPL_BASE, baseFile.toString()));
        artifacts.add(new GeneratedArtifact(implPath, ArtifactType.DELEGATE_IMPL, implFile.toString()));
        return artifacts;
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
