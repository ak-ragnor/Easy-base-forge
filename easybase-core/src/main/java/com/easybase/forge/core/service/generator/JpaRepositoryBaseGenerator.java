package com.easybase.forge.core.service.generator;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.model.ArtifactType;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class JpaRepositoryBaseGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.persistenceBasePackage(config);
		String jpaRepoBaseName = ServiceGeneratorUtils.jpaRepositoryBaseName(config);
		String entityClassName = ServiceGeneratorUtils.entityClassName(config);
		String entityPkg = ServiceGeneratorUtils.domainEntityPackage(config);

		ClassName entityType = ClassName.get(entityPkg, entityClassName);
		ClassName jpaRepository = ClassName.get("org.springframework.data.jpa.repository", "JpaRepository");
		ParameterizedTypeName superInterface =
				ParameterizedTypeName.get(jpaRepository, entityType, ServiceGeneratorUtils.resolveIdType(config));

		TypeSpec.Builder builder = TypeSpec.interfaceBuilder(jpaRepoBaseName)
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(
						AnnotationSpec.builder(ClassName.get("org.springframework.data.repository", "NoRepositoryBean"))
								.build())
				.addSuperinterface(superInterface)
				.addJavadoc(
						"Generated Spring Data JPA repository base for {@code $L}.\n\n"
								+ "<p>Used internally by {@code $LPersistenceAdapterBase}. "
								+ "Do not inject this interface into service-layer beans.\n\n"
								+ "<p>This file is always regenerated — add custom JPQL queries "
								+ "to {@code $LJpaRepository} instead.\n",
						entityClassName,
						config.getEntity(),
						config.getEntity());

		if (config.getResolvedSoftDelete().enabled()) {
			builder.addMethod(buildFindActiveByIdMethod(config, entityType));
			builder.addMethod(buildFindAllActiveMethod(config, entityType));
		}

		ServiceGeneratorUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(jpaRepoBaseName + ".java");

		return List.of(
				new GeneratedArtifact(outputPath, ArtifactType.SERVICE_JPA_REPOSITORY_BASE, javaFile.toString()));
	}

	private MethodSpec buildFindActiveByIdMethod(ServiceConfig config, ClassName entityType) {
		String entityClassName = ServiceGeneratorUtils.entityClassName(config);
		ParameterizedTypeName returnType = ParameterizedTypeName.get(ClassName.get(Optional.class), entityType);

		AnnotationSpec queryAnnotation = AnnotationSpec.builder(
						ClassName.get("org.springframework.data.jpa.repository", "Query"))
				.addMember(
						"value", "$S", "SELECT e FROM " + entityClassName + " e WHERE e.id = :id AND e.deleted = false")
				.build();

		return MethodSpec.methodBuilder("findActiveById")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addAnnotation(queryAnnotation)
				.addParameter(ServiceGeneratorUtils.resolveIdType(config), "id")
				.returns(returnType)
				.build();
	}

	private MethodSpec buildFindAllActiveMethod(ServiceConfig config, ClassName entityType) {
		String entityClassName = ServiceGeneratorUtils.entityClassName(config);
		ParameterizedTypeName returnType = ParameterizedTypeName.get(ClassName.get(List.class), entityType);

		AnnotationSpec queryAnnotation = AnnotationSpec.builder(
						ClassName.get("org.springframework.data.jpa.repository", "Query"))
				.addMember("value", "$S", "SELECT e FROM " + entityClassName + " e WHERE e.deleted = false")
				.build();

		return MethodSpec.methodBuilder("findAllActive")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addAnnotation(queryAnnotation)
				.returns(returnType)
				.build();
	}
}
