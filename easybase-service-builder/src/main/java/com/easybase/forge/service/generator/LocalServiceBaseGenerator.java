package com.easybase.forge.service.generator;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.util.PackageUtils;
import com.easybase.forge.service.config.ServiceConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class LocalServiceBaseGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServicePackageUtils.serviceBasePackage(config);
		String localServiceBaseName = ServiceNamingUtils.localServiceBaseName(config);
		String entityName = ServiceNamingUtils.entityName(config);
		String modelPkg = ServicePackageUtils.domainModelPackage(config);

		ClassName domainType = ClassName.get(modelPkg, entityName);
		com.squareup.javapoet.TypeName idType = ServiceTypeResolver.resolveIdType(config);
		ClassName baseServiceType = ClassName.get("com.easybase.service.runtime", "BaseService");
		ParameterizedTypeName baseServiceParamType = ParameterizedTypeName.get(baseServiceType, domainType, idType);

		TypeSpec.Builder builder = TypeSpec.interfaceBuilder(localServiceBaseName)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(baseServiceParamType)
				.addJavadoc(
						"Generated local service base interface for {@code $L}.\n\n"
								+ "<p>Extends {@code BaseService} independently of {@code $LServiceBase}.\n"
								+ "Extend {@code $LLocalService} to add business-specific methods.\n"
								+ "This interface is always regenerated — do not edit it.\n",
						entityName,
						entityName,
						entityName);

		ServiceMetaUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), pkg)
				.resolve(localServiceBaseName + ".java");

		return List.of(
				new GeneratedArtifact(outputPath, ServiceArtifactType.SERVICE_LOCAL_SERVICE_BASE, javaFile.toString()));
	}
}
