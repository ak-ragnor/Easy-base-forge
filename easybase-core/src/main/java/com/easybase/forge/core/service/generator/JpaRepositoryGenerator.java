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

/**
 * Generates the Spring Data JPA repository interface for the configured entity.
 *
 * <p>This interface is the only place where Spring Data JPA appears. It is used internally
 * by {@code UserPersistenceAdapter} and is never referenced by the service layer directly.
 *
 * <p>When {@code audit.softDelete = true}, custom JPQL finder methods that filter on
 * {@code deleted = false} are added to support the soft-delete behaviour expected by
 * the persistence adapter.
 *
 * <p>Example output for entity {@code User} with soft-delete:
 * <pre>
 * {@literal @}NoRepositoryBean
 * public interface UserJpaRepository extends JpaRepository&lt;UserEntity, UUID&gt; {
 *
 *     {@literal @}Query("SELECT e FROM UserEntity e WHERE e.id = :id AND e.deleted = false")
 *     Optional&lt;UserEntity&gt; findActiveById(UUID id);
 *
 *     {@literal @}Query("SELECT e FROM UserEntity e WHERE e.deleted = false")
 *     List&lt;UserEntity&gt; findAllActive();
 * }
 * </pre>
 */
public class JpaRepositoryGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.persistencePackage(config);
		String jpaRepoName = ServiceGeneratorUtils.jpaRepositoryName(config);
		String entityClassName = ServiceGeneratorUtils.entityClassName(config);
		String entityName = ServiceGeneratorUtils.entityName(config);

		ClassName entityType = ClassName.get(pkg, entityClassName);
		ClassName jpaRepository = ClassName.get("org.springframework.data.jpa.repository", "JpaRepository");
		ParameterizedTypeName superInterface =
				ParameterizedTypeName.get(jpaRepository, entityType, ServiceGeneratorUtils.resolveIdType(config));

		TypeSpec.Builder builder = TypeSpec.interfaceBuilder(jpaRepoName)
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(
						AnnotationSpec.builder(ClassName.get("org.springframework.data.repository", "NoRepositoryBean"))
								.build())
				.addSuperinterface(superInterface)
				.addJavadoc(
						"Spring Data JPA repository for {@code $L}.\n\n"
								+ "<p>Used internally by {@code $LPersistenceAdapter}. "
								+ "Do not inject this repository into service-layer beans.\n"
								+ "Add custom JPQL or native queries here.\n",
						entityClassName,
						entityName);

		if (config.getResolvedAudit().softDelete()) {
			builder.addMethod(buildFindActiveByIdMethod(config, entityType));
			builder.addMethod(buildFindAllActiveMethod(config, entityType));
		}

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(jpaRepoName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_JPA_REPOSITORY, javaFile.toString()));
	}

	private MethodSpec buildFindActiveByIdMethod(ServiceConfig config, ClassName entityType) {
		String entityClassName = ServiceGeneratorUtils.entityClassName(config);
		ParameterizedTypeName returnType = ParameterizedTypeName.get(ClassName.get(Optional.class), entityType);

		AnnotationSpec queryAnnotation = AnnotationSpec.builder(
						ClassName.get("org.springframework.data.jpa.repository", "Query"))
				.addMember(
						"value",
						"$S",
						"SELECT e FROM " + entityClassName + " e WHERE e.id = :id AND e."
								+ config.getResolvedAudit().softDeleteColumn() + " = false")
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
				.addMember(
						"value",
						"$S",
						"SELECT e FROM " + entityClassName + " e WHERE e."
								+ config.getResolvedAudit().softDeleteColumn() + " = false")
				.build();

		return MethodSpec.methodBuilder("findAllActive")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addAnnotation(queryAnnotation)
				.returns(returnType)
				.build();
	}
}
