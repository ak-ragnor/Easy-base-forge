package com.easybase.forge.core.service.generator;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.model.ArtifactType;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class LocalServiceBaseGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.serviceBasePackage(config);
		String localServiceBaseName = ServiceGeneratorUtils.localServiceBaseName(config);
		String entityName = ServiceGeneratorUtils.entityName(config);
		String modelPkg = ServiceGeneratorUtils.domainModelPackage(config);

		ClassName domainType = ClassName.get(modelPkg, entityName);
		com.squareup.javapoet.TypeName idType = ServiceGeneratorUtils.resolveIdType(config);
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

		ServiceGeneratorUtils.applyAuthors(builder, config);
		TypeSpec localServiceBase = builder.build();

		JavaFile javaFile = JavaFile.builder(pkg, localServiceBase)
				.skipJavaLangImports(true)
				.build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(localServiceBaseName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_LOCAL_SERVICE_BASE, javaFile.toString()));
	}
}
