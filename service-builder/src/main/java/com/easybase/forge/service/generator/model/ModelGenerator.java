package com.easybase.forge.service.generator.model;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.util.PackageUtils;
import com.easybase.forge.service.config.ServiceConfig;
import com.easybase.forge.service.generator.ServiceArtifactGenerator;
import com.easybase.forge.service.generator.ServiceArtifactType;
import com.easybase.forge.service.generator.ServiceMetaUtils;
import com.easybase.forge.service.generator.ServiceNamingUtils;
import com.easybase.forge.service.generator.ServicePackageUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

public class ModelGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServicePackageUtils.domainModelPackage(config);
		String entityName = ServiceNamingUtils.entityName(config);
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

		ServiceMetaUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), pkg)
				.resolve(entityName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ServiceArtifactType.SERVICE_MODEL, javaFile.toString()));
	}
}
