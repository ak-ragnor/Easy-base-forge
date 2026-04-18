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

public class RepositoryGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.repositoryPackage(config);
		String repoName = ServiceGeneratorUtils.repositoryName(config);
		String repoBasePkg = ServiceGeneratorUtils.repositoryBasePackage(config);
		String repoBaseName = ServiceGeneratorUtils.repositoryBaseName(config);
		String entityName = ServiceGeneratorUtils.entityName(config);

		ClassName repoBaseType = ClassName.get(repoBasePkg, repoBaseName);

		TypeSpec.Builder builder = TypeSpec.interfaceBuilder(repoName)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(repoBaseType)
				.addJavadoc(
						"Developer-owned repository interface for {@code $L}.\n\n"
								+ "<p>Add custom domain query methods here. "
								+ "The service layer injects this interface — "
								+ "{@code $LPersistenceAdapter} provides the implementation.\n\n"
								+ "<p>This file is generated once and never overwritten.\n",
						entityName,
						entityName);

		ServiceGeneratorUtils.applyAuthors(builder, config);
		TypeSpec repoInterface = builder.build();

		JavaFile javaFile =
				JavaFile.builder(pkg, repoInterface).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);

		Path outputPath = outputDir.resolve(repoName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_REPOSITORY, javaFile.toString()));
	}
}
