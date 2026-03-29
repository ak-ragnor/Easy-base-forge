package com.easybase.forge.core.service.generator;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.model.ArtifactType;
import com.easybase.forge.core.service.config.ResolvedAuditConfig;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

/**
 * Generates the {@code @MappedSuperclass} base entity containing audit fields.
 *
 * <p>Generated only when {@code audit.enabled = true}. All JPA entities for entities
 * with audit enabled extend this class rather than declaring audit fields individually.
 *
 * <p>The {@code createdBy} and {@code updatedBy} field types are driven by
 * {@code audit.auditorType} ({@code UUID}, {@code String}, or {@code Long}).
 * The {@code deleted} soft-delete flag is included only when {@code audit.softDelete = true}.
 *
 * <p>Timestamps ({@code createdAt}, {@code updatedAt}) are managed automatically by
 * Hibernate's {@code @CreationTimestamp} and {@code @UpdateTimestamp}.
 * The auditor fields ({@code createdBy}, {@code updatedBy}) must be populated manually,
 * typically in a {@link com.easybase.service.runtime.BaseHook} implementation.
 *
 * <p>Returns an empty list when {@code audit.enabled = false}.
 */
public class BaseEntityGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		ResolvedAuditConfig audit = config.getResolvedAudit();

		if (!audit.enabled()) {
			return Collections.emptyList();
		}

		String pkg = ServiceGeneratorUtils.persistencePackage(config);

		TypeSpec baseEntity = buildBaseEntity(config, audit, pkg);

		JavaFile javaFile =
				JavaFile.builder(pkg, baseEntity).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve("BaseEntity.java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_BASE_ENTITY, javaFile.toString()));
	}

	private TypeSpec buildBaseEntity(ServiceConfig config, ResolvedAuditConfig audit, String pkg) {
		ClassName mappedSuperclass = ClassName.get("jakarta.persistence", "MappedSuperclass");
		ClassName getter = ClassName.get("lombok", "Getter");
		ClassName setter = ClassName.get("lombok", "Setter");

		TypeSpec.Builder builder = TypeSpec.classBuilder("BaseEntity")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addAnnotation(AnnotationSpec.builder(mappedSuperclass).build())
				.addAnnotation(AnnotationSpec.builder(getter).build())
				.addAnnotation(AnnotationSpec.builder(setter).build())
				.addJavadoc("Abstract JPA base entity providing audit fields for all generated entities.\n\n"
						+ "<p>Timestamps are managed automatically by Hibernate. "
						+ "Auditor fields ({@code createdBy}, {@code updatedBy}) must be\n"
						+ "populated in a {@code @Component} implementing the entity's hook interface.\n");

		builder.addField(buildIdField(config));
		builder.addField(buildCreatedAtField());
		builder.addField(buildCreatedByField(audit));
		builder.addField(buildUpdatedAtField());
		builder.addField(buildUpdatedByField(audit));

		if (audit.softDelete()) {
			builder.addField(buildDeletedField(audit));
		}

		return builder.build();
	}

	private FieldSpec buildIdField(ServiceConfig config) {
		ClassName idAnnotation = ClassName.get("jakarta.persistence", "Id");

		if ("Long".equals(config.getIdType())) {
			ClassName generatedValue = ClassName.get("jakarta.persistence", "GeneratedValue");
			ClassName generationType = ClassName.get("jakarta.persistence", "GenerationType");

			return FieldSpec.builder(Long.class, "id", Modifier.PRIVATE)
					.addAnnotation(idAnnotation)
					.addAnnotation(AnnotationSpec.builder(generatedValue)
							.addMember("strategy", "$T.IDENTITY", generationType)
							.build())
					.build();
		}

		return FieldSpec.builder(UUID.class, "id", Modifier.PRIVATE)
				.addAnnotation(idAnnotation)
				.initializer("$T.randomUUID()", UUID.class)
				.build();
	}

	private FieldSpec buildCreatedAtField() {
		ClassName creationTimestamp = ClassName.get("org.hibernate.annotations", "CreationTimestamp");
		ClassName column = ClassName.get("jakarta.persistence", "Column");

		return FieldSpec.builder(Instant.class, "createdAt", Modifier.PRIVATE)
				.addAnnotation(AnnotationSpec.builder(creationTimestamp).build())
				.addAnnotation(AnnotationSpec.builder(column)
						.addMember("name", "$S", "created_at")
						.addMember("updatable", "false")
						.build())
				.build();
	}

	private FieldSpec buildCreatedByField(ResolvedAuditConfig audit) {
		ClassName column = ClassName.get("jakarta.persistence", "Column");
		com.squareup.javapoet.TypeName auditorType = ServiceGeneratorUtils.resolveType(audit.auditorType(), "");

		return FieldSpec.builder(auditorType, "createdBy", Modifier.PRIVATE)
				.addAnnotation(AnnotationSpec.builder(column)
						.addMember("name", "$S", "created_by")
						.addMember("updatable", "false")
						.build())
				.build();
	}

	private FieldSpec buildUpdatedAtField() {
		ClassName updateTimestamp = ClassName.get("org.hibernate.annotations", "UpdateTimestamp");
		ClassName column = ClassName.get("jakarta.persistence", "Column");

		return FieldSpec.builder(Instant.class, "updatedAt", Modifier.PRIVATE)
				.addAnnotation(AnnotationSpec.builder(updateTimestamp).build())
				.addAnnotation(AnnotationSpec.builder(column)
						.addMember("name", "$S", "updated_at")
						.build())
				.build();
	}

	private FieldSpec buildUpdatedByField(ResolvedAuditConfig audit) {
		ClassName column = ClassName.get("jakarta.persistence", "Column");
		com.squareup.javapoet.TypeName auditorType = ServiceGeneratorUtils.resolveType(audit.auditorType(), "");

		return FieldSpec.builder(auditorType, "updatedBy", Modifier.PRIVATE)
				.addAnnotation(AnnotationSpec.builder(column)
						.addMember("name", "$S", "updated_by")
						.build())
				.build();
	}

	private FieldSpec buildDeletedField(ResolvedAuditConfig audit) {
		ClassName column = ClassName.get("jakarta.persistence", "Column");

		return FieldSpec.builder(Boolean.class, "deleted", Modifier.PRIVATE)
				.addAnnotation(AnnotationSpec.builder(column)
						.addMember("name", "$S", audit.softDeleteColumn())
						.addMember("nullable", "false")
						.build())
				.initializer("false")
				.build();
	}
}
