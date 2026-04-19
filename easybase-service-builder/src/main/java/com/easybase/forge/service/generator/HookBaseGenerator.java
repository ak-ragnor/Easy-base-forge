package com.easybase.forge.service.generator;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.util.PackageUtils;
import com.easybase.forge.service.config.ServiceConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class HookBaseGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		if (!config.getHook().isEnabled()) {
			return Collections.emptyList();
		}

		String pkg = ServicePackageUtils.hookBasePackage(config);
		String hookBaseName = ServiceNamingUtils.hookBaseName(config);
		String modelPkg = ServicePackageUtils.domainModelPackage(config);
		String entityName = ServiceNamingUtils.entityName(config);

		ClassName domainType = ClassName.get(modelPkg, entityName);
		TypeName idType = ServiceTypeResolver.resolveIdType(config);

		TypeSpec.Builder builder = TypeSpec.interfaceBuilder(hookBaseName)
				.addModifiers(Modifier.PUBLIC)
				.addJavadoc(
						"Lifecycle hook base interface for {@link $T}.\n\n"
								+ "<p>All methods have default no-op implementations. "
								+ "Implement this interface as a Spring {@code @Component}\n"
								+ "(via the generated {@code $LHook} class) "
								+ "and override only the lifecycle events you need.\n\n"
								+ "<p>Hooks are invoked by {@code $LPersistenceAdapterBase} "
								+ "around save and delete operations.\n\n"
								+ "<p>This file is always regenerated — do not edit it.\n",
						domainType,
						entityName,
						entityName)
				.addMethod(buildDefaultMethod(
						"beforeSave",
						domainType,
						"entity",
						"Called before saving a new or updated entity to the database."))
				.addMethod(buildDefaultMethod(
						"afterSave",
						domainType,
						"entity",
						"Called after saving a new or updated entity to the database."))
				.addMethod(buildDefaultMethod(
						"beforeUpdate", domainType, "entity", "Called before an explicit update operation."))
				.addMethod(buildDefaultMethod(
						"afterUpdate", domainType, "entity", "Called after an explicit update operation."))
				.addMethod(buildDefaultMethod(
						"beforeDelete", idType, "id", "Called before deleting (or soft-deleting) an entity."))
				.addMethod(buildDefaultMethod(
						"afterDelete", idType, "id", "Called after deleting (or soft-deleting) an entity."));

		ServiceMetaUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), pkg)
				.resolve(hookBaseName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ServiceArtifactType.SERVICE_HOOK_BASE, javaFile.toString()));
	}

	private MethodSpec buildDefaultMethod(String name, TypeName paramType, String paramName, String javadoc) {
		return MethodSpec.methodBuilder(name)
				.addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
				.addParameter(paramType, paramName)
				.addJavadoc(javadoc + "\n")
				.build();
	}
}
