package com.easybase.forge.core.generator.delegate;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.generator.ArtifactGenerator;
import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.generator.GeneratorUtils;
import com.easybase.forge.core.generator.TypeNameResolver;
import com.easybase.forge.core.model.*;
import com.squareup.javapoet.*;

/**
 * Generates the {@code {Resource}ApiDelegate} interface for a resource.
 *
 * <p>One method per endpoint, return types governed by the generator config
 * (either {@link com.easybase.forge.core.config.ResponseEntityMode} or a custom
 * response wrapper when {@code generate.responseWrapper.enabled: true}).
 */
public class DelegateGenerator implements ArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ApiResource resource, GeneratorConfig config) {
		String delegatePkg =
				config.resolvePackage(config.getStructure().getDelegate().getPkg(), resource.packageSuffix());
		String dtoPkg = config.resolvePackage(config.getStructure().getDto().getPkg(), resource.packageSuffix());
		TypeNameResolver typeResolver = new TypeNameResolver(dtoPkg);

		String delegateName = resource.name() + "ApiDelegate";

		TypeSpec.Builder interfaceBuilder =
				TypeSpec.interfaceBuilder(delegateName).addModifiers(Modifier.PUBLIC);

		GeneratorUtils.addGeneratedJavadoc(interfaceBuilder, config);

		for (ApiEndpoint endpoint : resource.endpoints()) {
			MethodSpec method = buildMethod(endpoint, typeResolver, config);
			interfaceBuilder.addMethod(method);
		}

		JavaFile javaFile = JavaFile.builder(delegatePkg, interfaceBuilder.build())
				.skipJavaLangImports(true)
				.indent("    ")
				.build();

		Path outputPath = GeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), delegatePkg)
				.resolve(delegateName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.DELEGATE, javaFile.toString()));
	}

	private MethodSpec buildMethod(ApiEndpoint endpoint, TypeNameResolver typeResolver, GeneratorConfig config) {
		TypeName returnType = typeResolver.resolveReturnType(
				endpoint,
				config.getGenerate().getResponseEntityWrapping(),
				config.getGenerate().getResponseWrapper(),
				config.getGenerate().getPagination());

		MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(endpoint.operationId())
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.returns(returnType);

		GeneratorUtils.addEndpointParameters(methodBuilder, endpoint, typeResolver, config);

		return methodBuilder.build();
	}
}
