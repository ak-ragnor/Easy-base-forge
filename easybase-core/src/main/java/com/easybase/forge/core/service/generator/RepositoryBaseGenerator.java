package com.easybase.forge.core.service.generator;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.model.ArtifactType;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class RepositoryBaseGenerator implements ServiceArtifactGenerator {

	private static final String BASE_REPOSITORY_PACKAGE = "com.easybase.service.runtime";
	private static final String BASE_REPOSITORY_CLASS = "BaseRepository";

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.repositoryBasePackage(config);
		String repoBaseName = ServiceGeneratorUtils.repositoryBaseName(config);
		String modelPkg = ServiceGeneratorUtils.domainModelPackage(config);
		String entityName = ServiceGeneratorUtils.entityName(config);

		ClassName entityType = ClassName.get(modelPkg, entityName);
		ClassName baseRepo = ClassName.get(BASE_REPOSITORY_PACKAGE, BASE_REPOSITORY_CLASS);
		ParameterizedTypeName superInterface =
				ParameterizedTypeName.get(baseRepo, entityType, ServiceGeneratorUtils.resolveIdType(config));

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

		ServiceGeneratorUtils.applyAuthors(builder, config);
		TypeSpec repoBase = builder.build();

		JavaFile javaFile =
				JavaFile.builder(pkg, repoBase).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(repoBaseName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_REPOSITORY_BASE, javaFile.toString()));
	}
}
