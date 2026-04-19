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

public class JpaRepositoryGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServicePackageUtils.jpaRepositoryPackage(config);
		String jpaRepoName = ServiceNamingUtils.jpaRepositoryName(config);
		String jpaRepoBasePkg = ServicePackageUtils.jpaRepositoryBasePackage(config);
		String jpaRepoBaseName = ServiceNamingUtils.jpaRepositoryBaseName(config);
		String entityName = ServiceNamingUtils.entityName(config);

		ClassName jpaRepoBaseType = ClassName.get(jpaRepoBasePkg, jpaRepoBaseName);

		TypeSpec.Builder builder = TypeSpec.interfaceBuilder(jpaRepoName)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(jpaRepoBaseType)
				.addJavadoc(
						"Developer-owned Spring Data JPA repository for {@code $L}.\n\n"
								+ "<p>Add custom JPQL or native query methods here. "
								+ "This interface is used internally by {@code $LPersistenceAdapterBase}.\n\n"
								+ "<p>This file is generated once and never overwritten.\n",
						entityName,
						entityName);

		ServiceMetaUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), pkg)
				.resolve(jpaRepoName + ".java");

		return List.of(
				new GeneratedArtifact(outputPath, ServiceArtifactType.SERVICE_JPA_REPOSITORY, javaFile.toString()));
	}
}
