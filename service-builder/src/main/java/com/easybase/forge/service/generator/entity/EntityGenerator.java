package com.easybase.forge.service.generator.entity;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.lang.model.element.Modifier;

import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.util.PackageUtils;
import com.easybase.forge.service.config.RelationType;
import com.easybase.forge.service.config.RelationshipConfig;
import com.easybase.forge.service.config.ResolvedAuditConfig;
import com.easybase.forge.service.config.ServiceConfig;
import com.easybase.forge.service.config.ServiceField;
import com.easybase.forge.service.generator.ServiceArtifactGenerator;
import com.easybase.forge.service.generator.ServiceArtifactType;
import com.easybase.forge.service.generator.ServiceMetaUtils;
import com.easybase.forge.service.generator.ServiceNamingUtils;
import com.easybase.forge.service.generator.ServicePackageUtils;
import com.easybase.forge.service.generator.ServiceTableUtils;
import com.easybase.forge.service.generator.ServiceTypeResolver;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class EntityGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServicePackageUtils.domainEntityPackage(config);
		String entityClassName = ServiceNamingUtils.entityClassName(config);
		String tableName = ServiceTableUtils.tableName(config);
		ResolvedAuditConfig audit = config.getResolvedAudit();

		TypeSpec.Builder builder = TypeSpec.classBuilder(entityClassName)
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Entity"))
						.build())
				.addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Table"))
						.addMember("name", "$S", tableName)
						.build())
				.addAnnotation(AnnotationSpec.builder(ClassName.get("lombok", "Getter"))
						.build())
				.addAnnotation(AnnotationSpec.builder(ClassName.get("lombok", "Setter"))
						.build())
				.addAnnotation(AnnotationSpec.builder(ClassName.get("lombok", "NoArgsConstructor"))
						.build())
				.addAnnotation(AnnotationSpec.builder(ClassName.get("lombok", "AllArgsConstructor"))
						.build())
				.addAnnotation(AnnotationSpec.builder(ClassName.get("lombok", "EqualsAndHashCode"))
						.build())
				.addJavadoc(
						"JPA entity for the {@code $L} table.\n\n"
								+ "<p>This class is always regenerated — do not edit it directly.\n"
								+ "Custom queries belong in {@code $LJpaRepository}.\n",
						tableName,
						config.getEntity());

		builder.addField(buildIdField(config));

		for (ServiceField field : config.getFields()) {
			builder.addField(buildColumnField(field, config));
		}

		addRelationshipFields(builder, config);

		if (audit.enabled()) {
			addAuditFields(builder, audit);
		}

		if (config.getResolvedSoftDelete().enabled()) {
			builder.addField(buildDeletedField());
		}

		if (config.getResolvedTenant().enabled()) {
			builder.addField(buildTenantIdField(config));
		}

		ServiceMetaUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), pkg)
				.resolve(entityClassName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ServiceArtifactType.SERVICE_ENTITY, javaFile.toString()));
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

	private FieldSpec buildColumnField(ServiceField field, ServiceConfig config) {
		TypeName fieldType = ServiceTypeResolver.resolve(field.getType(), config.getBasePackage());
		AnnotationSpec.Builder columnBuilder = AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Column"))
				.addMember("name", "$S", ServiceTableUtils.toSnakeCase(field.getName()));

		if (field.isRequired()) {
			columnBuilder.addMember("nullable", "false");
		}

		if (field.getMaxLength() != null) {
			columnBuilder.addMember("length", "$L", field.getMaxLength());
		}

		if (field.isUnique()) {
			columnBuilder.addMember("unique", "true");
		}

		return FieldSpec.builder(fieldType, field.getName(), Modifier.PRIVATE)
				.addAnnotation(columnBuilder.build())
				.build();
	}

	private void addRelationshipFields(TypeSpec.Builder builder, ServiceConfig config) {
		for (RelationshipConfig rel : config.getRelationships()) {
			if (rel.getType() == RelationType.ONE_TO_ONE) {
				builder.addField(buildForeignKeyField(rel));
			} else if (rel.getType() == RelationType.MANY_TO_ONE) {
				System.err.println("[EasyBase] WARNING: MANY_TO_ONE relationships are not supported. "
						+ "Skipping relationship with column '" + rel.getColumn() + "'. "
						+ "Use ONE_TO_ONE instead.");
			}
		}
	}

	private FieldSpec buildForeignKeyField(RelationshipConfig rel) {
		String fieldName = snakeToCamelCase(rel.getColumn());
		TypeName idType = resolveRelIdType(rel.getIdType());

		return FieldSpec.builder(idType, fieldName, Modifier.PRIVATE)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Column"))
						.addMember("name", "$S", rel.getColumn())
						.addMember("nullable", "$L", rel.isNullable())
						.build())
				.build();
	}

	private TypeName resolveRelIdType(String idType) {
		if (idType == null || idType.isBlank() || "UUID".equals(idType)) return ClassName.get(UUID.class);
		if ("Long".equals(idType)) return ClassName.get(Long.class);
		if ("String".equals(idType)) return ClassName.get(String.class);
		return ClassName.get(UUID.class);
	}

	private void addAuditFields(TypeSpec.Builder builder, ResolvedAuditConfig audit) {
		TypeName auditorType = ServiceTypeResolver.resolve(audit.auditorType(), "");

		builder.addField(FieldSpec.builder(Instant.class, "createdAt", Modifier.PRIVATE)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("org.hibernate.annotations", "CreationTimestamp"))
						.build())
				.addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Column"))
						.addMember("name", "$S", "created_at")
						.addMember("updatable", "false")
						.build())
				.build());

		builder.addField(FieldSpec.builder(auditorType, "createdBy", Modifier.PRIVATE)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Column"))
						.addMember("name", "$S", "created_by")
						.addMember("updatable", "false")
						.build())
				.build());

		builder.addField(FieldSpec.builder(Instant.class, "updatedAt", Modifier.PRIVATE)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("org.hibernate.annotations", "UpdateTimestamp"))
						.build())
				.addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Column"))
						.addMember("name", "$S", "updated_at")
						.build())
				.build());

		builder.addField(FieldSpec.builder(auditorType, "updatedBy", Modifier.PRIVATE)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Column"))
						.addMember("name", "$S", "updated_by")
						.build())
				.build());
	}

	private FieldSpec buildDeletedField() {
		return FieldSpec.builder(Boolean.class, "deleted", Modifier.PRIVATE)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Column"))
						.addMember("name", "$S", "deleted")
						.build())
				.initializer("$L", false)
				.build();
	}

	private FieldSpec buildTenantIdField(ServiceConfig config) {
		TypeName tenantIdType =
				ServiceTypeResolver.resolve(config.getResolvedTenant().tenantIdType(), "");

		return FieldSpec.builder(tenantIdType, "tenantId", Modifier.PRIVATE)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Column"))
						.addMember("name", "$S", "tenant_id")
						.build())
				.build();
	}

	private String snakeToCamelCase(String snake) {
		StringBuilder result = new StringBuilder();
		boolean nextUpper = false;
		for (char c : snake.toCharArray()) {
			if (c == '_') {
				nextUpper = true;
			} else if (nextUpper) {
				result.append(Character.toUpperCase(c));
				nextUpper = false;
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}
}
