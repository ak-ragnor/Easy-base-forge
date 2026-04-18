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

public class ServiceGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.servicePackage(config);
		String serviceName = ServiceGeneratorUtils.serviceName(config);
		String serviceBaseImplPkg = ServiceGeneratorUtils.serviceBasePackage(config);
		String serviceBaseImplName = ServiceGeneratorUtils.serviceBaseImplName(config);
		String localServiceName = ServiceGeneratorUtils.localServiceName(config);
		String localServiceFieldName = ServiceGeneratorUtils.fieldName(config, "LocalService");
		String entityName = ServiceGeneratorUtils.entityName(config);

		ClassName serviceBaseImplType = ClassName.get(serviceBaseImplPkg, serviceBaseImplName);
		ClassName localServiceType = ClassName.get(pkg, localServiceName);

		TypeSpec.Builder builder = TypeSpec.classBuilder(serviceName)
				.addModifiers(Modifier.PUBLIC)
				.superclass(serviceBaseImplType)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("org.springframework.stereotype", "Component"))
						.build())
				.addJavadoc(
						"Developer-owned service façade for {@code $L}.\n\n"
								+ "<p>Add cross-cutting concerns (security checks, caching, event publishing) here.\n"
								+ "All CRUD operations are delegated to {@code $LLocalServiceBase} via the base class.\n\n"
								+ "<p>This file is generated once and never overwritten.\n",
						entityName,
						entityName)
				.addMethod(buildConstructor(localServiceType, localServiceFieldName));

		ServiceGeneratorUtils.applyAuthors(builder, config);
		TypeSpec service = builder.build();

		JavaFile javaFile =
				JavaFile.builder(pkg, service).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(serviceName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_SERVICE, javaFile.toString()));
	}

	private MethodSpec buildConstructor(ClassName localServiceType, String localServiceFieldName) {
		return MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(localServiceType, localServiceFieldName)
				.addStatement("super($L)", localServiceFieldName)
				.build();
	}
}
