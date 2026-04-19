package com.easybase.forge.service.generator.model;

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
import com.easybase.forge.service.generator.ServiceTypeResolver;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class ModelBaseGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServicePackageUtils.domainModelPackage(config);
		String entityName = ServiceNamingUtils.entityName(config);
		String baseClassName = entityName + "Base";
		ResolvedAuditConfig audit = config.getResolvedAudit();

		TypeSpec.Builder builder = TypeSpec.classBuilder(baseClassName)
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("lombok", "Getter"))
						.build())
				.addAnnotation(AnnotationSpec.builder(ClassName.get("lombok", "Setter"))
						.build())
				.addAnnotation(AnnotationSpec.builder(ClassName.get("lombok", "NoArgsConstructor"))
						.build())
				.addAnnotation(AnnotationSpec.builder(ClassName.get("lombok", "AllArgsConstructor"))
						.build())
				.addJavadoc(
						"Generated base domain model for {@code $L}.\n\n"
								+ "<p>Contains all generated fields. "
								+ "Do not edit — this file is always regenerated.\n"
								+ "Add custom fields in the subclass {@code $L}.\n",
						entityName,
						entityName);

		builder.addField(buildIdField(config));

		for (ServiceField field : config.getFields()) {
			builder.addField(buildDeclaredField(field, config));
		}

		for (RelationshipConfig rel : config.getRelationships()) {
			if (rel.getType() == RelationType.ONE_TO_ONE) {
				builder.addField(buildForeignKeyField(rel));
			}
		}

		if (audit.enabled()) {
			addAuditFields(builder, audit);
		}

		if (config.getResolvedSoftDelete().enabled()) {
			builder.addField(FieldSpec.builder(Boolean.class, "deleted", Modifier.PRIVATE)
					.build());
		}

		if (config.getResolvedTenant().enabled()) {
			TypeName tenantIdType =
					ServiceTypeResolver.resolve(config.getResolvedTenant().tenantIdType(), "");
			builder.addField(FieldSpec.builder(tenantIdType, "tenantId", Modifier.PRIVATE)
					.build());
		}

		ServiceMetaUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), pkg)
				.resolve(baseClassName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ServiceArtifactType.SERVICE_MODEL_BASE, javaFile.toString()));
	}

	private FieldSpec buildIdField(ServiceConfig config) {
		TypeName idType = ServiceTypeResolver.resolveIdType(config);
		return FieldSpec.builder(idType, "id", Modifier.PRIVATE).build();
	}

	private FieldSpec buildDeclaredField(ServiceField field, ServiceConfig config) {
		TypeName fieldType = ServiceTypeResolver.resolve(field.getType(), config.getBasePackage());
		return FieldSpec.builder(fieldType, field.getName(), Modifier.PRIVATE).build();
	}

	private FieldSpec buildForeignKeyField(RelationshipConfig rel) {
		String fieldName = snakeToCamelCase(rel.getColumn());
		TypeName idType = resolveRelIdType(rel.getIdType());
		return FieldSpec.builder(idType, fieldName, Modifier.PRIVATE).build();
	}

	private TypeName resolveRelIdType(String idType) {
		if (idType == null || idType.isBlank() || "UUID".equals(idType)) return ClassName.get(UUID.class);
		if ("Long".equals(idType)) return ClassName.get(Long.class);
		if ("String".equals(idType)) return ClassName.get(String.class);
		return ClassName.get(UUID.class);
	}

	private void addAuditFields(TypeSpec.Builder builder, ResolvedAuditConfig audit) {
		TypeName auditorType = ServiceTypeResolver.resolve(audit.auditorType(), "");
		builder.addField(
				FieldSpec.builder(Instant.class, "createdAt", Modifier.PRIVATE).build());
		builder.addField(
				FieldSpec.builder(auditorType, "createdBy", Modifier.PRIVATE).build());
		builder.addField(
				FieldSpec.builder(Instant.class, "updatedAt", Modifier.PRIVATE).build());
		builder.addField(
				FieldSpec.builder(auditorType, "updatedBy", Modifier.PRIVATE).build());
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
