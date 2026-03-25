package com.easybase.forge.core.generator.delegate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.config.PaginationMode;
import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.generator.GeneratorUtils;
import com.easybase.forge.core.generator.TypeNameResolver;
import com.easybase.forge.core.model.*;
import com.squareup.javapoet.*;

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

	private static final ClassName COMPONENT = ClassName.get("org.springframework.stereotype", "Component");

	public List<GeneratedArtifact> generate(ApiResource resource, GeneratorConfig config) {
		String delegatePkg =
				config.resolvePackage(config.getStructure().getDelegate().getPkg(), resource.packageSuffix());
		String dtoPkg = config.resolvePackage(config.getStructure().getDto().getPkg(), resource.packageSuffix());
		TypeNameResolver typeResolver = new TypeNameResolver(dtoPkg);

		String implPkg = delegatePkg + ".impl";
		String basePkg = implPkg + ".base";

		String delegateName = resource.name() + "ApiDelegate";
		String baseName = resource.name() + "ApiDelegateImplBase";
		String implName = resource.name() + "ApiDelegateImpl";

		ClassName delegateType = ClassName.get(delegatePkg, delegateName);
		ClassName baseType = ClassName.get(basePkg, baseName);

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

	private MethodSpec buildStubMethod(ApiEndpoint endpoint, TypeNameResolver typeResolver, GeneratorConfig config) {
		boolean applyPagination =
				endpoint.paginated() && config.getGenerate().getPagination() == PaginationMode.SPRING_DATA;

		TypeName returnType = typeResolver.resolveReturnType(
				endpoint,
				config.getGenerate().getResponseEntityWrapping(),
				config.getGenerate().getResponseWrapper(),
				config.getGenerate().getPagination());

		MethodSpec.Builder mb = MethodSpec.methodBuilder(endpoint.operationId())
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(returnType);

		for (ApiParameter param : endpoint.parameters()) {
			if (param.in() == ParameterLocation.PATH || param.in() == ParameterLocation.QUERY) {
				mb.addParameter(
						typeResolver.resolve(param.schema().javaType()), GeneratorUtils.sanitizeName(param.name()));
			}
		}

		if (endpoint.requestBody() != null) {
			String bodyType = endpoint.requestBody().schema().javaType();
			mb.addParameter(typeResolver.resolve(bodyType), GeneratorUtils.deriveBodyParamName(bodyType));
		}

		if (applyPagination) {
			mb.addParameter(TypeNameResolver.pageableType(), "pageable");
		}

		mb.addStatement("throw new $T($S)", UnsupportedOperationException.class, "Not implemented");

		return mb.build();
	}
}
