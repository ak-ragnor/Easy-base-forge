package com.easybase.forge.core.service.generator;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.model.ArtifactType;
import com.easybase.forge.core.service.config.CrudOptions;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class ServiceBaseGenerator implements ServiceArtifactGenerator {

	private static final String BASE_SERVICE_PACKAGE = "com.easybase.service.runtime";
	private static final String BASE_SERVICE_CLASS = "BaseService";

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.serviceBasePackage(config);
		String serviceBaseName = ServiceGeneratorUtils.serviceBaseName(config);
		String modelPkg = ServiceGeneratorUtils.domainModelPackage(config);
		String entityName = ServiceGeneratorUtils.entityName(config);

		ClassName entityType = ClassName.get(modelPkg, entityName);
		ClassName baseService = ClassName.get(BASE_SERVICE_PACKAGE, BASE_SERVICE_CLASS);
		ParameterizedTypeName superInterface =
				ParameterizedTypeName.get(baseService, entityType, ServiceGeneratorUtils.resolveIdType(config));

		TypeSpec.Builder builder = TypeSpec.interfaceBuilder(serviceBaseName)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(superInterface)
				.addJavadoc(
						"Generated base service interface for {@link $T}.\n\n"
								+ "<p>Extend {@code $LLocalService} to add business-specific methods.\n"
								+ "This interface is always regenerated — do not edit it.\n",
						entityType,
						entityName);

		addDisabledCrudWarnings(builder, config.getCrud());
		ServiceGeneratorUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(serviceBaseName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_SERVICE_BASE, javaFile.toString()));
	}

	private void addDisabledCrudWarnings(TypeSpec.Builder builder, CrudOptions crud) {
		StringBuilder doc = new StringBuilder();

		if (!crud.isCreate()) {
			doc.append("<p><b>Disabled:</b> {@code create} — see easybase.yml.\n");
		}

		if (!crud.isUpdate()) {
			doc.append("<p><b>Disabled:</b> {@code update} — see easybase.yml.\n");
		}

		if (!crud.isDelete()) {
			doc.append("<p><b>Disabled:</b> {@code delete} — see easybase.yml.\n");
		}

		if (!crud.isGet()) {
			doc.append("<p><b>Disabled:</b> {@code findById} — see easybase.yml.\n");
		}

		if (!crud.isList()) {
			doc.append("<p><b>Disabled:</b> {@code findAll} — see easybase.yml.\n");
		}

		if (doc.length() > 0) {
			builder.addJavadoc(doc.toString());
		}
	}
}
