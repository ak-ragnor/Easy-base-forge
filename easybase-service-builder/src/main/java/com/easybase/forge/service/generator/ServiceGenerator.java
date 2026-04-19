package com.easybase.forge.service.generator;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.util.PackageUtils;
import com.easybase.forge.service.config.ServiceConfig;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class ServiceGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServicePackageUtils.servicePackage(config);
		String serviceName = ServiceNamingUtils.serviceName(config);
		String serviceBaseImplPkg = ServicePackageUtils.serviceBasePackage(config);
		String serviceBaseImplName = ServiceNamingUtils.serviceBaseImplName(config);
		String localServiceName = ServiceNamingUtils.localServiceName(config);
		String localServiceFieldName = ServiceNamingUtils.fieldName(config, "LocalService");
		String entityName = ServiceNamingUtils.entityName(config);

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

		ServiceMetaUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), pkg)
				.resolve(serviceName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ServiceArtifactType.SERVICE_SERVICE, javaFile.toString()));
	}

	private MethodSpec buildConstructor(ClassName localServiceType, String localServiceFieldName) {
		return MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(localServiceType, localServiceFieldName)
				.addStatement("super($L)", localServiceFieldName)
				.build();
	}
}
