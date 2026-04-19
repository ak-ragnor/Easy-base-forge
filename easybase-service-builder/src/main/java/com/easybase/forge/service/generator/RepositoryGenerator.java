package com.easybase.forge.service.generator;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.util.PackageUtils;
import com.easybase.forge.service.config.ServiceConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

public class RepositoryGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServicePackageUtils.repositoryPackage(config);
		String repoName = ServiceNamingUtils.repositoryName(config);
		String repoBasePkg = ServicePackageUtils.repositoryBasePackage(config);
		String repoBaseName = ServiceNamingUtils.repositoryBaseName(config);
		String entityName = ServiceNamingUtils.entityName(config);

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

		ServiceMetaUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), pkg)
				.resolve(repoName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ServiceArtifactType.SERVICE_REPOSITORY, javaFile.toString()));
	}
}
