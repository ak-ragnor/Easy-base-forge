package com.easybase.forge.core.generator.controller;

import java.nio.file.Path;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.generator.GeneratorUtils;
import com.easybase.forge.core.model.ApiResource;
import com.easybase.forge.core.model.ArtifactType;
import com.squareup.javapoet.*;

/**
 * Generates the user-owned {@code {Resource}Controller} class.
 *
 * <p>This file is created only once — it is never overwritten on regeneration.
 * It extends the generated base controller and delegates everything to it.
 *
 * <p>Optional annotations ({@code @CrossOrigin}, {@code @Slf4j}) are added when
 * the corresponding config options are set.
 */
public class CustomControllerGenerator {

	private static final ClassName REST_CONTROLLER =
			ClassName.get("org.springframework.web.bind.annotation", "RestController");
	private static final ClassName CROSS_ORIGIN =
			ClassName.get("org.springframework.web.bind.annotation", "CrossOrigin");
	private static final ClassName SLF4J = ClassName.get("lombok.extern.slf4j", "Slf4j");

	public GeneratedArtifact generate(ApiResource resource, GeneratorConfig config) {
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

		Path outputPath = GeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), controllerPkg)
				.resolve(controllerName + ".java");

		return new GeneratedArtifact(outputPath, ArtifactType.CUSTOM_CONTROLLER, javaFile.toString());
	}
}
