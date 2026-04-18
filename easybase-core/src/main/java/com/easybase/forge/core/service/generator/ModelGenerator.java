package com.easybase.forge.core.service.generator;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.model.ArtifactType;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

public class ModelGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.domainModelPackage(config);
		String entityName = ServiceGeneratorUtils.entityName(config);
		String baseClassName = entityName + "Base";

		ClassName baseType = ClassName.get(pkg, baseClassName);

		TypeSpec.Builder builder = TypeSpec.classBuilder(entityName)
				.addModifiers(Modifier.PUBLIC)
				.superclass(baseType)
				.addJavadoc(
						"Developer-owned domain model for {@code $L}.\n\n"
								+ "<p>Extends {@link $T} which is always regenerated. "
								+ "Add custom fields and methods here.\n\n"
								+ "<p>This file is generated once and never overwritten.\n",
						entityName,
						baseType);

		ServiceGeneratorUtils.applyAuthors(builder, config);
		TypeSpec modelClass = builder.build();

		JavaFile javaFile =
				JavaFile.builder(pkg, modelClass).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(entityName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_MODEL, javaFile.toString()));
	}
}
