package com.easybase.forge.service.generator;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.util.PackageUtils;
import com.easybase.forge.service.config.CrudOptions;
import com.easybase.forge.service.config.ServiceConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class ServiceBaseGenerator implements ServiceArtifactGenerator {

	private static final String BASE_SERVICE_PACKAGE = "com.easybase.service.runtime";
	private static final String BASE_SERVICE_CLASS = "BaseService";

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServicePackageUtils.serviceBasePackage(config);
		String serviceBaseName = ServiceNamingUtils.serviceBaseName(config);
		String modelPkg = ServicePackageUtils.domainModelPackage(config);
		String entityName = ServiceNamingUtils.entityName(config);

		ClassName entityType = ClassName.get(modelPkg, entityName);
		ClassName baseService = ClassName.get(BASE_SERVICE_PACKAGE, BASE_SERVICE_CLASS);
		ParameterizedTypeName superInterface =
				ParameterizedTypeName.get(baseService, entityType, ServiceTypeResolver.resolveIdType(config));

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
		ServiceMetaUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), pkg)
				.resolve(serviceBaseName + ".java");

		return List.of(
				new GeneratedArtifact(outputPath, ServiceArtifactType.SERVICE_SERVICE_BASE, javaFile.toString()));
	}

	private void addDisabledCrudWarnings(TypeSpec.Builder builder, CrudOptions crud) {
		StringBuilder doc = new StringBuilder();

		if (!crud.isCreate()) doc.append("<p><b>Disabled:</b> {@code create} — see easybase.yml.\n");
		if (!crud.isUpdate()) doc.append("<p><b>Disabled:</b> {@code update} — see easybase.yml.\n");
		if (!crud.isDelete()) doc.append("<p><b>Disabled:</b> {@code delete} — see easybase.yml.\n");
		if (!crud.isGet()) doc.append("<p><b>Disabled:</b> {@code findById} — see easybase.yml.\n");
		if (!crud.isList()) doc.append("<p><b>Disabled:</b> {@code findAll} — see easybase.yml.\n");

		if (doc.length() > 0) {
			builder.addJavadoc(doc.toString());
		}
	}
}
