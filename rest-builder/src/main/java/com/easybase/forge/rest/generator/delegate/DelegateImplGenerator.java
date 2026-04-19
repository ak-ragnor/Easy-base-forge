package com.easybase.forge.rest.generator.delegate;

import java.nio.file.Path;
import java.util.ArrayList;
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
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class DelegateImplGenerator implements RestArtifactGenerator {

	private static final ClassName COMPONENT = ClassName.get("org.springframework.stereotype", "Component");

	@Override
	public List<GeneratedArtifact> generate(ApiResource resource, GeneratorConfig config) {
		if (!config.getGenerate().isDelegateImpl()) {
			return List.of();
		}

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

		Path basePath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), basePkg)
				.resolve(baseName + ".java");

		TypeSpec.Builder implBuilder = TypeSpec.classBuilder(implName)
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(COMPONENT)
				.superclass(baseType);

		JavaFile implFile = JavaFile.builder(implPkg, implBuilder.build())
				.skipJavaLangImports(true)
				.indent("    ")
				.build();

		Path implPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), implPkg)
				.resolve(implName + ".java");

		List<GeneratedArtifact> artifacts = new ArrayList<>();
		artifacts.add(new GeneratedArtifact(basePath, RestArtifactType.DELEGATE_IMPL_BASE, baseFile.toString()));
		artifacts.add(new GeneratedArtifact(implPath, RestArtifactType.DELEGATE_IMPL, implFile.toString()));

		return artifacts;
	}

	private MethodSpec buildStubMethod(ApiEndpoint endpoint, TypeNameResolver typeResolver, GeneratorConfig config) {
		TypeName returnType = typeResolver.resolveReturnType(
				endpoint,
				config.getGenerate().getResponseEntityWrapping(),
				config.getGenerate().getResponseWrapper(),
				config.getGenerate().getPagination());

		MethodSpec.Builder mb = MethodSpec.methodBuilder(endpoint.operationId())
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(returnType);

		RestGeneratorUtils.addEndpointParameters(mb, endpoint, typeResolver, config);

		mb.addStatement("throw new $T($S)", UnsupportedOperationException.class, "Not implemented");

		return mb.build();
	}
}
