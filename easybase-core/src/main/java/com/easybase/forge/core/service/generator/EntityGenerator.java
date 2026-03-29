package com.easybase.forge.core.service.generator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.model.ArtifactType;
import com.easybase.forge.core.service.config.CascadeStrategy;
import com.easybase.forge.core.service.config.FetchStrategy;
import com.easybase.forge.core.service.config.RelationType;
import com.easybase.forge.core.service.config.RelationshipConfig;
import com.easybase.forge.core.service.config.ResolvedAuditConfig;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.easybase.forge.core.service.config.ServiceField;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Generates the JPA {@code @Entity} class for the configured entity.
 *
 * <p>When {@code audit.enabled = true}, the entity extends the generated {@code BaseEntity}
 * (which provides the {@code @Id} field and audit columns). Otherwise, an {@code @Id} field
 * is added directly.
 *
 * <p>Each {@link ServiceField} becomes a {@code @Column}-annotated Lombok field with
 * appropriate constraints ({@code nullable}, {@code length}, {@code unique}).
 *
 * <p>Relationships are mapped as JPA annotations:
 * <ul>
 *   <li>{@code MANY_TO_ONE} and {@code ONE_TO_ONE}: {@code @ManyToOne}/{@code @OneToOne} +
 *       {@code @JoinColumn} with a named {@code @ForeignKey} constraint.</li>
 *   <li>{@code ONE_TO_MANY}: {@code @OneToMany} with {@code mappedBy}; also generates
 *       {@code add}/{@code remove} helper methods.</li>
 * </ul>
 */
public class EntityGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.persistencePackage(config);
		String entityClassName = ServiceGeneratorUtils.entityClassName(config);
		String tableName = ServiceGeneratorUtils.tableName(config);
		ResolvedAuditConfig audit = config.getResolvedAudit();

		TypeSpec.Builder builder = TypeSpec.classBuilder(entityClassName)
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(buildEntityAnnotation())
				.addAnnotation(buildTableAnnotation(tableName))
				.addAnnotation(
						AnnotationSpec.builder(ClassName.get("lombok", "Data")).build())
				.addAnnotation(AnnotationSpec.builder(ClassName.get("lombok", "NoArgsConstructor"))
						.build())
				.addAnnotation(AnnotationSpec.builder(ClassName.get("lombok", "AllArgsConstructor"))
						.build())
				.addJavadoc(
						"JPA entity for the {@code $L} table.\n\n"
								+ "<p>This class is always regenerated — do not edit it directly.\n"
								+ "Custom queries belong in {@code $LJpaRepository}.\n",
						tableName,
						config.getEntity());

		if (audit.enabled()) {
			ClassName baseEntityType = ClassName.get(pkg, "BaseEntity");
			builder.superclass(baseEntityType);
			builder.addAnnotation(AnnotationSpec.builder(ClassName.get("lombok", "EqualsAndHashCode"))
					.addMember("callSuper", "true")
					.build());
		} else {
			builder.addField(buildStandaloneIdField(config));
		}

		addFieldSpecs(builder, config);
		addRelationshipFields(builder, config);
		addOneToManyHelperMethods(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(entityClassName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_ENTITY, javaFile.toString()));
	}

	private AnnotationSpec buildEntityAnnotation() {
		return AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Entity"))
				.build();
	}

	private AnnotationSpec buildTableAnnotation(String tableName) {
		return AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Table"))
				.addMember("name", "$S", tableName)
				.build();
	}

	private FieldSpec buildStandaloneIdField(ServiceConfig config) {
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

	private void addFieldSpecs(TypeSpec.Builder builder, ServiceConfig config) {
		for (ServiceField field : config.getFields()) {
			builder.addField(buildColumnField(field, config));
		}
	}

	private FieldSpec buildColumnField(ServiceField field, ServiceConfig config) {
		TypeName fieldType = ServiceGeneratorUtils.resolveType(field.getType(), config.getBasePackage());

		AnnotationSpec.Builder columnBuilder = AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Column"))
				.addMember("name", "$S", ServiceGeneratorUtils.toSnakeCase(field.getName()));

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
			if (rel.getType() == RelationType.MANY_TO_ONE) {
				builder.addField(buildManyToOneField(rel, config));
			} else if (rel.getType() == RelationType.ONE_TO_ONE) {
				builder.addField(buildOneToOneField(rel, config));
			} else if (rel.getType() == RelationType.ONE_TO_MANY) {
				builder.addField(buildOneToManyField(rel, config));
			}
		}
	}

	private FieldSpec buildManyToOneField(RelationshipConfig rel, ServiceConfig config) {
		String relatedEntityClass = rel.getEntity() + "Entity";
		ClassName relatedType = ClassName.get(ServiceGeneratorUtils.persistencePackage(config), relatedEntityClass);

		String column = resolveColumn(rel);
		String tableName = ServiceGeneratorUtils.tableName(config);
		String foreignKey = resolveForeignKey(rel, tableName, column);

		AnnotationSpec manyToOne = AnnotationSpec.builder(ClassName.get("jakarta.persistence", "ManyToOne"))
				.addMember("fetch", "$T.$L", ClassName.get("jakarta.persistence", "FetchType"), resolveFetch(rel))
				.build();

		AnnotationSpec joinColumn = AnnotationSpec.builder(ClassName.get("jakarta.persistence", "JoinColumn"))
				.addMember("name", "$S", column)
				.addMember("nullable", "$L", rel.isNullable())
				.addMember(
						"foreignKey", "@$T(name = $S)", ClassName.get("jakarta.persistence", "ForeignKey"), foreignKey)
				.build();

		FieldSpec.Builder fieldBuilder = FieldSpec.builder(relatedType, rel.getField(), Modifier.PRIVATE)
				.addAnnotation(manyToOne)
				.addAnnotation(joinColumn);

		addCascadeAnnotations(fieldBuilder, rel);

		return fieldBuilder.build();
	}

	private FieldSpec buildOneToOneField(RelationshipConfig rel, ServiceConfig config) {
		String relatedEntityClass = rel.getEntity() + "Entity";
		ClassName relatedType = ClassName.get(ServiceGeneratorUtils.persistencePackage(config), relatedEntityClass);

		String column = resolveColumn(rel);
		String tableName = ServiceGeneratorUtils.tableName(config);
		String foreignKey = resolveForeignKey(rel, tableName, column);

		AnnotationSpec oneToOne = AnnotationSpec.builder(ClassName.get("jakarta.persistence", "OneToOne"))
				.addMember("fetch", "$T.$L", ClassName.get("jakarta.persistence", "FetchType"), resolveFetch(rel))
				.build();

		AnnotationSpec joinColumn = AnnotationSpec.builder(ClassName.get("jakarta.persistence", "JoinColumn"))
				.addMember("name", "$S", column)
				.addMember("nullable", "$L", rel.isNullable())
				.addMember(
						"foreignKey", "@$T(name = $S)", ClassName.get("jakarta.persistence", "ForeignKey"), foreignKey)
				.build();

		FieldSpec.Builder fieldBuilder = FieldSpec.builder(relatedType, rel.getField(), Modifier.PRIVATE)
				.addAnnotation(oneToOne)
				.addAnnotation(joinColumn);

		addCascadeAnnotations(fieldBuilder, rel);

		return fieldBuilder.build();
	}

	private FieldSpec buildOneToManyField(RelationshipConfig rel, ServiceConfig config) {
		String relatedEntityClass = rel.getEntity() + "Entity";
		ClassName relatedType = ClassName.get(ServiceGeneratorUtils.persistencePackage(config), relatedEntityClass);
		ParameterizedTypeName listType = ParameterizedTypeName.get(ClassName.get("java.util", "List"), relatedType);

		AnnotationSpec.Builder oneToManyBuilder = AnnotationSpec.builder(
						ClassName.get("jakarta.persistence", "OneToMany"))
				.addMember("fetch", "$T.$L", ClassName.get("jakarta.persistence", "FetchType"), resolveFetch(rel));

		if (rel.getMappedBy() != null && !rel.getMappedBy().isBlank()) {
			oneToManyBuilder.addMember("mappedBy", "$S", rel.getMappedBy());
		}

		addCascadeMembers(oneToManyBuilder, rel);

		return FieldSpec.builder(listType, rel.getField(), Modifier.PRIVATE)
				.addAnnotation(oneToManyBuilder.build())
				.initializer("new $T<>()", ClassName.get("java.util", "ArrayList"))
				.build();
	}

	private void addCascadeAnnotations(FieldSpec.Builder fieldBuilder, RelationshipConfig rel) {
		if (rel.getCascade().isEmpty()) {
			return;
		}

		List<String> cascadeValues = buildCascadeValueList(rel);

		if (cascadeValues.isEmpty()) {
			return;
		}

		AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.get("jakarta.persistence", "CascadeType"));

		for (String value : cascadeValues) {
			fieldBuilder.addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "CascadeType"))
					.build());
		}
	}

	private void addCascadeMembers(AnnotationSpec.Builder annotationBuilder, RelationshipConfig rel) {
		if (rel.getCascade().isEmpty()) {
			return;
		}

		ClassName cascadeType = ClassName.get("jakarta.persistence", "CascadeType");
		List<String> cascadeValues = buildCascadeValueList(rel);

		if (cascadeValues.isEmpty()) {
			return;
		}

		if (cascadeValues.size() == 1) {
			annotationBuilder.addMember("cascade", "$T.$L", cascadeType, cascadeValues.get(0));
		} else {
			StringBuilder format = new StringBuilder("{");
			List<Object> args = new ArrayList<>();

			for (int i = 0; i < cascadeValues.size(); i++) {
				if (i > 0) {
					format.append(", ");
				}

				format.append("$T.$L");
				args.add(cascadeType);
				args.add(cascadeValues.get(i));
			}

			format.append("}");
			annotationBuilder.addMember("cascade", format.toString(), args.toArray());
		}
	}

	private List<String> buildCascadeValueList(RelationshipConfig rel) {
		List<String> values = new ArrayList<>();

		for (CascadeStrategy strategy : rel.getCascade()) {
			values.add(strategy.name());
		}

		return values;
	}

	private String resolveFetch(RelationshipConfig rel) {
		if (rel.getFetch() == FetchStrategy.EAGER) {
			return "EAGER";
		}

		return "LAZY";
	}

	private String resolveColumn(RelationshipConfig rel) {
		if (rel.getColumn() != null && !rel.getColumn().isBlank()) {
			return rel.getColumn();
		}

		return ServiceGeneratorUtils.deriveForeignKeyColumn(rel.getField());
	}

	private String resolveForeignKey(RelationshipConfig rel, String tableName, String column) {
		if (rel.getForeignKey() != null && !rel.getForeignKey().isBlank()) {
			return rel.getForeignKey();
		}

		return ServiceGeneratorUtils.deriveForeignKeyName(tableName, column);
	}

	private void addOneToManyHelperMethods(TypeSpec.Builder builder, ServiceConfig config) {
		for (RelationshipConfig rel : config.getRelationships()) {
			if (rel.getType() == RelationType.ONE_TO_MANY) {
				builder.addMethod(buildAddHelperMethod(rel, config));
				builder.addMethod(buildRemoveHelperMethod(rel, config));
			}
		}
	}

	private MethodSpec buildAddHelperMethod(RelationshipConfig rel, ServiceConfig config) {
		String relatedEntityClass = rel.getEntity() + "Entity";
		ClassName relatedType = ClassName.get(ServiceGeneratorUtils.persistencePackage(config), relatedEntityClass);
		String paramName = Character.toLowerCase(rel.getEntity().charAt(0))
				+ rel.getEntity().substring(1);

		return MethodSpec.methodBuilder("add" + rel.getEntity())
				.addModifiers(Modifier.PUBLIC)
				.addParameter(relatedType, paramName)
				.addStatement("if ($L != null) { $L.add($L); }", paramName, rel.getField(), paramName)
				.build();
	}

	private MethodSpec buildRemoveHelperMethod(RelationshipConfig rel, ServiceConfig config) {
		String relatedEntityClass = rel.getEntity() + "Entity";
		ClassName relatedType = ClassName.get(ServiceGeneratorUtils.persistencePackage(config), relatedEntityClass);
		String paramName = Character.toLowerCase(rel.getEntity().charAt(0))
				+ rel.getEntity().substring(1);

		return MethodSpec.methodBuilder("remove" + rel.getEntity())
				.addModifiers(Modifier.PUBLIC)
				.addParameter(relatedType, paramName)
				.addStatement("$L.remove($L)", rel.getField(), paramName)
				.build();
	}
}
