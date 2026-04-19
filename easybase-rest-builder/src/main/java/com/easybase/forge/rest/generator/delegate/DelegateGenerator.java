package com.easybase.forge.rest.generator.delegate;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.common.config.GeneratorConfig;
import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.util.PackageUtils;
import com.easybase.forge.rest.generator.RestArtifactGenerator;
import com.easybase.forge.rest.generator.RestArtifactType;
import com.easybase.forge.rest.generator.RestGeneratorUtils;
import com.easybase.forge.rest.generator.TypeNameResolver;
import com.easybase.forge.rest.model.ApiEndpoint;
import com.easybase.forge.rest.model.ApiResource;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class DelegateGenerator implements RestArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ApiResource resource, GeneratorConfig config) {
		String delegatePkg =
				config.resolvePackage(config.getStructure().getDelegate().getPkg(), resource.packageSuffix());
		String dtoPkg = config.resolvePackage(config.getStructure().getDto().getPkg(), resource.packageSuffix());
		TypeNameResolver typeResolver = new TypeNameResolver(dtoPkg);

		String delegateName = resource.name() + "ApiDelegate";

		TypeSpec.Builder interfaceBuilder =
				TypeSpec.interfaceBuilder(delegateName).addModifiers(Modifier.PUBLIC);

		RestGeneratorUtils.addGeneratedJavadoc(interfaceBuilder, config);

		for (ApiEndpoint endpoint : resource.endpoints()) {
			interfaceBuilder.addMethod(buildMethod(endpoint, typeResolver, config));
		}

		JavaFile javaFile = JavaFile.builder(delegatePkg, interfaceBuilder.build())
				.skipJavaLangImports(true)
				.indent("    ")
				.build();

		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), delegatePkg)
				.resolve(delegateName + ".java");

		return List.of(new GeneratedArtifact(outputPath, RestArtifactType.DELEGATE, javaFile.toString()));
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

		RestGeneratorUtils.addEndpointParameters(methodBuilder, endpoint, typeResolver, config);

		return methodBuilder.build();
	}
}
