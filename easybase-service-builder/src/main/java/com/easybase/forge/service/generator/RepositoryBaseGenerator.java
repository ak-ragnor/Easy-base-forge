package com.easybase.forge.service.generator;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.util.PackageUtils;
import com.easybase.forge.service.config.ServiceConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class RepositoryBaseGenerator implements ServiceArtifactGenerator {

	private static final String BASE_REPOSITORY_PACKAGE = "com.easybase.service.runtime";
	private static final String BASE_REPOSITORY_CLASS = "BaseRepository";

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServicePackageUtils.repositoryBasePackage(config);
		String repoBaseName = ServiceNamingUtils.repositoryBaseName(config);
		String modelPkg = ServicePackageUtils.domainModelPackage(config);
		String entityName = ServiceNamingUtils.entityName(config);

		ClassName entityType = ClassName.get(modelPkg, entityName);
		ClassName baseRepo = ClassName.get(BASE_REPOSITORY_PACKAGE, BASE_REPOSITORY_CLASS);
		ParameterizedTypeName superInterface =
				ParameterizedTypeName.get(baseRepo, entityType, ServiceTypeResolver.resolveIdType(config));

		TypeSpec.Builder builder = TypeSpec.interfaceBuilder(repoBaseName)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(superInterface)
				.addJavadoc(
						"Generated base repository interface for {@link $T}.\n\n"
								+ "<p>The service layer depends only on this contract. "
								+ "The persistence implementation is in {@code $LPersistenceAdapter}.\n\n"
								+ "<p>This file is always regenerated — add custom query methods "
								+ "to {@code $LRepository} instead.\n",
						entityType,
						entityName,
						entityName);

		ServiceMetaUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), pkg)
				.resolve(repoBaseName + ".java");

		return List.of(
				new GeneratedArtifact(outputPath, ServiceArtifactType.SERVICE_REPOSITORY_BASE, javaFile.toString()));
	}
}
