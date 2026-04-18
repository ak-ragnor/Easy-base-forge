package com.easybase.forge.core.service.generator;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.model.ArtifactType;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class LocalServiceGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.servicePackage(config);
		String localServiceName = ServiceGeneratorUtils.localServiceName(config);
		String localBaseImplPkg = ServiceGeneratorUtils.serviceBasePackage(config);
		String localBaseImplName = ServiceGeneratorUtils.localServiceBaseImplName(config);
		String repoPkg = ServiceGeneratorUtils.repositoryPackage(config);
		String repoName = ServiceGeneratorUtils.repositoryName(config);
		String repoFieldName = ServiceGeneratorUtils.fieldName(config, "Repository");
		String entityName = ServiceGeneratorUtils.entityName(config);

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

		ServiceGeneratorUtils.applyAuthors(builder, config);
		TypeSpec localService = builder.build();

		JavaFile javaFile =
				JavaFile.builder(pkg, localService).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(localServiceName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_LOCAL_SERVICE, javaFile.toString()));
	}

	private MethodSpec buildConstructor(ClassName repoType, String repoFieldName) {
		return MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(repoType, repoFieldName)
				.addStatement("super($L)", repoFieldName)
				.build();
	}
}
