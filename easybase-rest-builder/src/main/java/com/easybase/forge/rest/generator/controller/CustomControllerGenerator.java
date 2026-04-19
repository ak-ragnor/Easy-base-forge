package com.easybase.forge.rest.generator.controller;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.common.config.GeneratorConfig;
import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.util.PackageUtils;
import com.easybase.forge.rest.generator.RestArtifactGenerator;
import com.easybase.forge.rest.generator.RestArtifactType;
import com.easybase.forge.rest.model.ApiResource;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class CustomControllerGenerator implements RestArtifactGenerator {

	private static final ClassName REST_CONTROLLER =
			ClassName.get("org.springframework.web.bind.annotation", "RestController");
	private static final ClassName CROSS_ORIGIN =
			ClassName.get("org.springframework.web.bind.annotation", "CrossOrigin");
	private static final ClassName SLF4J = ClassName.get("lombok.extern.slf4j", "Slf4j");

	@Override
	public List<GeneratedArtifact> generate(ApiResource resource, GeneratorConfig config) {
		String controllerPkg =
				config.resolvePackage(config.getStructure().getController().getPkg(), resource.packageSuffix());
		String basePkg =
				config.resolvePackage(config.getStructure().getController().getBasePkg(), resource.packageSuffix());
		String delegatePkg =
				config.resolvePackage(config.getStructure().getDelegate().getPkg(), resource.packageSuffix());

		String controllerName = resource.name() + "Controller";
		String baseName = resource.name() + "ControllerBase";
		String delegateName = resource.name() + "ApiDelegate";

		ClassName baseType = ClassName.get(basePkg, baseName);
		ClassName delegateType = ClassName.get(delegatePkg, delegateName);

		TypeSpec.Builder classBuilder = TypeSpec.classBuilder(controllerName)
				.addModifiers(Modifier.PUBLIC)
				.superclass(baseType)
				.addMethod(MethodSpec.constructorBuilder()
						.addModifiers(Modifier.PUBLIC)
						.addParameter(delegateType, "delegate")
						.addStatement("super(delegate)")
						.build());

		String crossOrigin = config.getGenerate().getCrossOrigin();

		if (crossOrigin != null && !crossOrigin.isBlank()) {
			classBuilder.addAnnotation(AnnotationSpec.builder(CROSS_ORIGIN)
					.addMember("origins", "$S", crossOrigin)
					.build());
		}

		if (config.getGenerate().isSlf4j()) {
			classBuilder.addAnnotation(SLF4J);
		}

		classBuilder.addAnnotation(REST_CONTROLLER);

		JavaFile javaFile = JavaFile.builder(controllerPkg, classBuilder.build())
				.skipJavaLangImports(true)
				.indent("    ")
				.build();

		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), controllerPkg)
				.resolve(controllerName + ".java");

		return List.of(new GeneratedArtifact(outputPath, RestArtifactType.CUSTOM_CONTROLLER, javaFile.toString()));
	}
}
