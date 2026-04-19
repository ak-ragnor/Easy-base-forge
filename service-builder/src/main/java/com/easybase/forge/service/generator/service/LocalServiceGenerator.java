package com.easybase.forge.service.generator.service;

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
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class LocalServiceGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServicePackageUtils.servicePackage(config);
		String localServiceName = ServiceNamingUtils.localServiceName(config);
		String localBaseImplPkg = ServicePackageUtils.serviceBasePackage(config);
		String localBaseImplName = ServiceNamingUtils.localServiceBaseImplName(config);
		String repoPkg = ServicePackageUtils.repositoryPackage(config);
		String repoName = ServiceNamingUtils.repositoryName(config);
		String repoFieldName = ServiceNamingUtils.fieldName(config, "Repository");
		String entityName = ServiceNamingUtils.entityName(config);

		ClassName localBaseImplType = ClassName.get(localBaseImplPkg, localBaseImplName);
		ClassName repoType = ClassName.get(repoPkg, repoName);

		TypeSpec.Builder builder = TypeSpec.classBuilder(localServiceName)
				.addModifiers(Modifier.PUBLIC)
				.superclass(localBaseImplType)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("org.springframework.stereotype", "Service"))
						.build())
				.addJavadoc(
						"Developer-owned local service for {@code $L}.\n\n"
								+ "<p>Add business logic and domain-specific operations here.\n"
								+ "CRUD operations are inherited from {@code $LLocalServiceBaseImpl}.\n\n"
								+ "<p>This file is generated once and never overwritten.\n",
						entityName,
						entityName)
				.addMethod(buildConstructor(repoType, repoFieldName));

		ServiceMetaUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), pkg)
				.resolve(localServiceName + ".java");

		return List.of(
				new GeneratedArtifact(outputPath, ServiceArtifactType.SERVICE_LOCAL_SERVICE, javaFile.toString()));
	}

	private MethodSpec buildConstructor(ClassName repoType, String repoFieldName) {
		return MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(repoType, repoFieldName)
				.addStatement("super($L)", repoFieldName)
				.build();
	}
}
