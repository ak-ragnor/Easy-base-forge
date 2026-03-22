package com.easybase.forge.core.generator.controller;

import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.generator.GeneratorUtils;
import com.easybase.forge.core.model.ApiResource;
import com.easybase.forge.core.model.ArtifactType;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;

/**
 * Generates the user-owned {@code {Resource}Controller} class.
 *
 * <p>This file is created only once — it is never overwritten on regeneration.
 * It extends the generated base controller and delegates everything to it.
 */
public class CustomControllerGenerator {

    private static final ClassName REST_CONTROLLER =
            ClassName.get("org.springframework.web.bind.annotation", "RestController");

    public GeneratedArtifact generate(ApiResource resource, GeneratorConfig config) {
        String controllerPkg = config.resolvePackage(
                config.getStructure().getController().getPkg(),
                resource.packageSuffix());
        String basePkg = config.resolvePackage(
                config.getStructure().getController().getBasePkg(),
                resource.packageSuffix());
        String delegatePkg = config.resolvePackage(
                config.getStructure().getDelegate().getPkg(),
                resource.packageSuffix());

        String controllerName = resource.name() + "Controller";
        String baseName = resource.name() + "ControllerBase";
        String delegateName = resource.name() + "ApiDelegate";

        ClassName baseType = ClassName.get(basePkg, baseName);
        ClassName delegateType = ClassName.get(delegatePkg, delegateName);

        TypeSpec typeSpec = TypeSpec.classBuilder(controllerName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(REST_CONTROLLER)
                .superclass(baseType)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(delegateType, "delegate")
                        .addStatement("super(delegate)")
                        .build())
                .build();

        JavaFile javaFile = JavaFile.builder(controllerPkg, typeSpec)
                .skipJavaLangImports(true)
                .indent("    ")
                .build();

        Path outputPath = GeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), controllerPkg)
                .resolve(controllerName + ".java");

        return new GeneratedArtifact(outputPath, ArtifactType.CUSTOM_CONTROLLER, javaFile.toString());
    }
}
