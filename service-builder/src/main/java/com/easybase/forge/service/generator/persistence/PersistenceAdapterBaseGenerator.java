package com.easybase.forge.service.generator.persistence;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.util.PackageUtils;
import com.easybase.forge.service.config.ServiceConfig;
import com.easybase.forge.service.generator.ServiceArtifactGenerator;
import com.easybase.forge.service.generator.ServiceArtifactType;
import com.easybase.forge.service.generator.ServiceMetaUtils;
import com.easybase.forge.service.generator.ServiceNamingUtils;
import com.easybase.forge.service.generator.ServicePackageUtils;
import com.easybase.forge.service.generator.ServiceTypeResolver;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class PersistenceAdapterBaseGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServicePackageUtils.persistenceBasePackage(config);
		String adapterBaseName = ServiceNamingUtils.persistenceAdapterBaseName(config);
		String entityName = ServiceNamingUtils.entityName(config);
		String modelPkg = ServicePackageUtils.domainModelPackage(config);
		String entityPkg = ServicePackageUtils.domainEntityPackage(config);
		String repoPkg = ServicePackageUtils.repositoryPackage(config);
		String repoName = ServiceNamingUtils.repositoryName(config);
		String jpaRepoPkg = ServicePackageUtils.jpaRepositoryPackage(config);
		String jpaRepoName = ServiceNamingUtils.jpaRepositoryName(config);

		ClassName domainType = ClassName.get(modelPkg, entityName);
		ClassName entityType = ClassName.get(entityPkg, entityName + "Entity");
		ClassName repoInterface = ClassName.get(repoPkg, repoName);
		ClassName jpaRepoType = ClassName.get(jpaRepoPkg, jpaRepoName);

		String jpaRepoFieldName = ServiceNamingUtils.fieldName(config, "JpaRepository");
		String hooksFieldName = ServiceNamingUtils.fieldName(config, "Hooks");

		TypeSpec.Builder builder = TypeSpec.classBuilder(adapterBaseName)
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addSuperinterface(repoInterface)
				.addJavadoc(
						"Abstract persistence adapter base for {@code $L}.\n\n"
								+ "<p>Bridges {@link $T} and Spring Data JPA. "
								+ "Implement {@code toDomain} and {@code toEntity} in {@code $LPersistenceAdapter}.\n\n"
								+ "<p>This file is always regenerated — do not edit it.\n",
						entityName,
						repoInterface,
						entityName);

		builder.addField(buildJpaRepoField(jpaRepoType, jpaRepoFieldName));

		if (config.getHook().isEnabled()) {
			builder.addField(buildHooksField(config, hooksFieldName));
		}

		builder.addMethod(buildConstructor(config, jpaRepoType, jpaRepoFieldName, hooksFieldName));
		builder.addMethod(buildCreateMethod(config, domainType, entityType, jpaRepoFieldName, hooksFieldName));
		builder.addMethod(buildUpdateMethod(config, domainType, entityType, jpaRepoFieldName, hooksFieldName));
		builder.addMethod(buildFindByIdMethod(config, domainType, jpaRepoFieldName));
		builder.addMethod(buildFindAllMethod(config, domainType, entityType, jpaRepoFieldName));

		if (config.getCrud().isDelete()) {
			builder.addMethod(buildDeleteByIdMethod(config, entityType, jpaRepoFieldName, hooksFieldName));
		}

		builder.addMethod(buildExistsByIdMethod(config, jpaRepoFieldName));
		builder.addMethod(buildToDomainMethod(domainType, entityType));
		builder.addMethod(buildToEntityMethod(domainType, entityType));

		ServiceMetaUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), pkg)
				.resolve(adapterBaseName + ".java");

		return List.of(new GeneratedArtifact(
				outputPath, ServiceArtifactType.SERVICE_PERSISTENCE_ADAPTER_BASE, javaFile.toString()));
	}

	private FieldSpec buildJpaRepoField(ClassName jpaRepoType, String fieldName) {
		return FieldSpec.builder(jpaRepoType, fieldName, Modifier.PROTECTED, Modifier.FINAL)
				.build();
	}

	private FieldSpec buildHooksField(ServiceConfig config, String fieldName) {
		String hookBasePkg = ServicePackageUtils.hookBasePackage(config);
		String hookBaseName = ServiceNamingUtils.hookBaseName(config);
		ClassName hookBaseType = ClassName.get(hookBasePkg, hookBaseName);
		ParameterizedTypeName hookListType = ParameterizedTypeName.get(ClassName.get(List.class), hookBaseType);
		return FieldSpec.builder(hookListType, fieldName, Modifier.PROTECTED, Modifier.FINAL)
				.build();
	}

	private MethodSpec buildConstructor(
			ServiceConfig config, ClassName jpaRepoType, String jpaRepoFieldName, String hooksFieldName) {
		MethodSpec.Builder builder =
				MethodSpec.constructorBuilder().addModifiers(Modifier.PROTECTED).addParameter(jpaRepoType, "jpaRepo");

		if (config.getHook().isEnabled()) {
			String hookBasePkg = ServicePackageUtils.hookBasePackage(config);
			String hookBaseName = ServiceNamingUtils.hookBaseName(config);
			ClassName hookBaseType = ClassName.get(hookBasePkg, hookBaseName);
			ParameterizedTypeName hookListType = ParameterizedTypeName.get(ClassName.get(List.class), hookBaseType);

			builder.addParameter(hookListType, "hooks");
			builder.addStatement("this.$L = jpaRepo", jpaRepoFieldName);
			builder.addStatement("this.$L = $T.emptyList()", hooksFieldName, ClassName.get("java.util", "Collections"));
			builder.addCode("\n");
			builder.beginControlFlow("if (hooks != null)");
			builder.addStatement("this.$L = hooks", hooksFieldName);
			builder.endControlFlow();
		} else {
			builder.addStatement("this.$L = jpaRepo", jpaRepoFieldName);
		}

		return builder.build();
	}

	private MethodSpec buildCreateMethod(
			ServiceConfig config, ClassName domainType, ClassName entityType, String jpaRepoField, String hooksField) {
		MethodSpec.Builder builder = MethodSpec.methodBuilder("create")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(domainType, "domain")
				.returns(domainType);

		if (config.getHook().isEnabled()) {
			addHookIteration(builder, config, "beforeSave", "domain", hooksField);
			builder.addCode("\n");
		}

		builder.addStatement("$T entity = toEntity(domain)", entityType);
		builder.addCode("\n");
		builder.addStatement("$T saved = toDomain($L.save(entity))", domainType, jpaRepoField);
		builder.addCode("\n");

		if (config.getHook().isEnabled()) {
			addHookIteration(builder, config, "afterSave", "saved", hooksField);
			builder.addCode("\n");
		}

		builder.addStatement("return saved");
		return builder.build();
	}

	private MethodSpec buildUpdateMethod(
			ServiceConfig config, ClassName domainType, ClassName entityType, String jpaRepoField, String hooksField) {
		TypeName idType = ServiceTypeResolver.resolveIdType(config);

		MethodSpec.Builder builder = MethodSpec.methodBuilder("update")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(idType, "id")
				.addParameter(domainType, "domain")
				.returns(domainType);

		if (config.getHook().isEnabled()) {
			addHookIteration(builder, config, "beforeUpdate", "domain", hooksField);
			builder.addCode("\n");
		}

		builder.addStatement("$T entity = toEntity(domain)", entityType);
		builder.addCode("\n");
		builder.addStatement("$T saved = toDomain($L.save(entity))", domainType, jpaRepoField);
		builder.addCode("\n");

		if (config.getHook().isEnabled()) {
			addHookIteration(builder, config, "afterUpdate", "saved", hooksField);
			builder.addCode("\n");
		}

		builder.addStatement("return saved");
		return builder.build();
	}

	private MethodSpec buildFindByIdMethod(ServiceConfig config, ClassName domainType, String jpaRepoField) {
		TypeName idType = ServiceTypeResolver.resolveIdType(config);
		ParameterizedTypeName returnType = ParameterizedTypeName.get(ClassName.get(Optional.class), domainType);

		MethodSpec.Builder builder = MethodSpec.methodBuilder("findById")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(idType, "id")
				.returns(returnType);

		if (config.getResolvedSoftDelete().enabled()) {
			builder.addStatement("return $L.findActiveById(id).map(this::toDomain)", jpaRepoField);
		} else {
			builder.addStatement("return $L.findById(id).map(this::toDomain)", jpaRepoField);
		}

		return builder.build();
	}

	private MethodSpec buildFindAllMethod(
			ServiceConfig config, ClassName domainType, ClassName entityType, String jpaRepoField) {
		ParameterizedTypeName returnType = ParameterizedTypeName.get(ClassName.get(List.class), domainType);
		ParameterizedTypeName entityListType = ParameterizedTypeName.get(ClassName.get(List.class), entityType);

		MethodSpec.Builder builder = MethodSpec.methodBuilder("findAll")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(returnType);

		if (config.getResolvedSoftDelete().enabled()) {
			builder.addStatement("$T entities = $L.findAllActive()", entityListType, jpaRepoField);
		} else {
			builder.addStatement("$T entities = $L.findAll()", entityListType, jpaRepoField);
		}

		builder.addCode("\n");
		builder.addStatement("$T result = new $T<>()", returnType, ClassName.get(ArrayList.class));
		builder.addCode("\n");
		builder.beginControlFlow("for ($T e : entities)", entityType);
		builder.addStatement("result.add(toDomain(e))");
		builder.endControlFlow();
		builder.addCode("\n");
		builder.addStatement("return result");

		return builder.build();
	}

	private MethodSpec buildDeleteByIdMethod(
			ServiceConfig config, ClassName entityType, String jpaRepoField, String hooksField) {
		TypeName idType = ServiceTypeResolver.resolveIdType(config);

		MethodSpec.Builder builder = MethodSpec.methodBuilder("deleteById")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(idType, "id");

		if (config.getHook().isEnabled()) {
			addHookIteration(builder, config, "beforeDelete", "id", hooksField);
			builder.addCode("\n");
		}

		if (config.getResolvedSoftDelete().enabled()) {
			builder.addStatement("$T entity = $L.findById(id).orElse(null)", entityType, jpaRepoField);
			builder.addCode("\n");
			builder.beginControlFlow("if (entity != null)");
			builder.addStatement("entity.setDeleted(true)");
			builder.addCode("\n");
			builder.addStatement("$L.save(entity)", jpaRepoField);
			builder.endControlFlow();
		} else {
			builder.addStatement("$L.deleteById(id)", jpaRepoField);
		}

		if (config.getHook().isEnabled()) {
			builder.addCode("\n");
			addHookIteration(builder, config, "afterDelete", "id", hooksField);
		}

		return builder.build();
	}

	private MethodSpec buildExistsByIdMethod(ServiceConfig config, String jpaRepoField) {
		TypeName idType = ServiceTypeResolver.resolveIdType(config);

		return MethodSpec.methodBuilder("existsById")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(idType, "id")
				.returns(boolean.class)
				.addStatement("return $L.existsById(id)", jpaRepoField)
				.build();
	}

	private MethodSpec buildToDomainMethod(ClassName domainType, ClassName entityType) {
		return MethodSpec.methodBuilder("toDomain")
				.addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT)
				.addParameter(entityType, "entity")
				.returns(domainType)
				.addJavadoc("Maps a JPA {@code $T} to the domain model {@link $T}.\n", entityType, domainType)
				.build();
	}

	private MethodSpec buildToEntityMethod(ClassName domainType, ClassName entityType) {
		return MethodSpec.methodBuilder("toEntity")
				.addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT)
				.addParameter(domainType, "domain")
				.returns(entityType)
				.addJavadoc("Maps the domain model {@link $T} to a JPA {@code $T}.\n", domainType, entityType)
				.build();
	}

	private void addHookIteration(
			MethodSpec.Builder method, ServiceConfig config, String hookMethod, String arg, String hooksField) {
		String hookBasePkg = ServicePackageUtils.hookBasePackage(config);
		String hookBaseName = ServiceNamingUtils.hookBaseName(config);
		ClassName hookBaseType = ClassName.get(hookBasePkg, hookBaseName);

		method.beginControlFlow("for ($T h : $L)", hookBaseType, hooksField);
		method.addStatement("h.$L($L)", hookMethod, arg);
		method.endControlFlow();
	}

	@SuppressWarnings("unused")
	private AnnotationSpec componentAnnotation() {
		return AnnotationSpec.builder(ClassName.get("org.springframework.stereotype", "Component"))
				.build();
	}
}
