package com.easybase.forge.core.service.generator;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.model.ArtifactType;
import com.easybase.forge.core.service.config.RelationType;
import com.easybase.forge.core.service.config.RelationshipConfig;
import com.easybase.forge.core.service.config.ResolvedAuditConfig;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.easybase.forge.core.service.config.ServiceField;

/**
 * Generates the immutable domain model record for the configured entity.
 *
 * <p>The generated record is the primary contract between the service layer
 * and the rest of the application. It never exposes JPA annotations or
 * persistence concerns.
 *
 * <p>For {@code MANY_TO_ONE} and {@code ONE_TO_ONE} relationships, the record
 * holds only the foreign key value (e.g. {@code UUID tenantId}) rather than the
 * full related domain object, keeping the domain model flat and self-contained.
 *
 * <p>{@code ONE_TO_MANY} relationships are excluded from the record — they represent
 * the non-owning side and are not needed by most service-layer operations.
 *
 * <p>Audit fields are appended at the end when {@code audit.enabled = true}.
 *
 * <p>Note: JavaPoet 1.13.0 does not support {@code TypeSpec.recordBuilder()}.
 * This generator builds the Java record source directly as a formatted string.
 */
public class ModelGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.modelPackage(config);
		String entityName = ServiceGeneratorUtils.entityName(config);
		ResolvedAuditConfig audit = config.getResolvedAudit();

		List<RecordComponent> components = buildRecordComponents(config, audit);
		Set<String> imports = resolveImports(components, audit);

		String source = buildSource(pkg, entityName, components, imports);

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(entityName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_MODEL, source));
	}

	private List<RecordComponent> buildRecordComponents(ServiceConfig config, ResolvedAuditConfig audit) {
		List<RecordComponent> components = new ArrayList<>();

		String idTypeName = resolveSimpleTypeName(
				ServiceGeneratorUtils.resolveIdType(config).toString());
		components.add(new RecordComponent(idTypeName, "id"));

		for (ServiceField field : config.getFields()) {
			String typeName =
					resolveSimpleTypeName(ServiceGeneratorUtils.resolveType(field.getType(), config.getBasePackage())
							.toString());
			components.add(new RecordComponent(typeName, field.getName()));
		}

		for (RelationshipConfig rel : config.getRelationships()) {
			if (isOwningRelationship(rel)) {
				String fkFieldName = deriveForeignKeyFieldName(rel);
				components.add(new RecordComponent("UUID", fkFieldName));
			}
		}

		if (audit.enabled()) {
			appendAuditComponents(components, audit);
		}

		return components;
	}

	/**
	 * Returns {@code true} for relationship types where this entity owns the foreign key.
	 */
	private boolean isOwningRelationship(RelationshipConfig rel) {
		if (rel.getType() == RelationType.MANY_TO_ONE) {
			return true;
		}

		if (rel.getType() == RelationType.ONE_TO_ONE) {
			return true;
		}

		return false;
	}

	/**
	 * Derives the FK field name for the domain record from the relationship config.
	 *
	 * <p>If the relationship has an explicit {@code column} set, strip the {@code _id} suffix
	 * and convert to camelCase. Otherwise, use {@code fieldName + "Id"}.
	 */
	private String deriveForeignKeyFieldName(RelationshipConfig rel) {
		if (rel.getColumn() != null && !rel.getColumn().isBlank()) {
			return snakeToCamelCase(rel.getColumn());
		}

		return rel.getField() + "Id";
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

	private void appendAuditComponents(List<RecordComponent> components, ResolvedAuditConfig audit) {
		components.add(new RecordComponent("Instant", "createdAt"));
		components.add(new RecordComponent(audit.auditorType(), "createdBy"));
		components.add(new RecordComponent("Instant", "updatedAt"));
		components.add(new RecordComponent(audit.auditorType(), "updatedBy"));

		if (audit.softDelete()) {
			components.add(new RecordComponent("Boolean", "deleted"));
		}
	}

	private Set<String> resolveImports(List<RecordComponent> components, ResolvedAuditConfig audit) {
		Set<String> imports = new LinkedHashSet<>();

		for (RecordComponent component : components) {
			String fqcn = resolveFullyQualifiedName(component.typeName());
			if (fqcn != null) {
				imports.add(fqcn);
			}
		}

		return imports;
	}

	/**
	 * Maps simple type names to their fully-qualified import path.
	 * Returns {@code null} for types in {@code java.lang} or primitives (no import needed).
	 */
	private String resolveFullyQualifiedName(String typeName) {
		if ("UUID".equals(typeName)) {
			return UUID.class.getName();
		}

		if ("Instant".equals(typeName)) {
			return Instant.class.getName();
		}

		return null;
	}

	/**
	 * Strips the package prefix from a fully-qualified class name if present.
	 */
	private String resolveSimpleTypeName(String fqcn) {
		int lastDot = fqcn.lastIndexOf('.');

		if (lastDot < 0) {
			return fqcn;
		}

		return fqcn.substring(lastDot + 1);
	}

	private String buildSource(String pkg, String entityName, List<RecordComponent> components, Set<String> imports) {
		StringBuilder sb = new StringBuilder();

		sb.append("package ").append(pkg).append(";\n");

		if (!imports.isEmpty()) {
			sb.append("\n");
			for (String imp : imports) {
				sb.append("import ").append(imp).append(";\n");
			}
		}

		sb.append("\n");
		sb.append("/**\n");
		sb.append(" * Immutable domain model for {@code ").append(entityName).append("}.\n");
		sb.append(" *\n");
		sb.append(" * <p>This record is the primary contract of the service layer. ");
		sb.append("Do not expose JPA entities ({@code ").append(entityName).append("Entity}) through service APIs.\n");
		sb.append(" *\n");
		sb.append(" * <p>This file is always regenerated — do not edit it.\n");
		sb.append(" */\n");
		sb.append("public record ").append(entityName).append("(\n");

		for (int i = 0; i < components.size(); i++) {
			RecordComponent comp = components.get(i);
			sb.append("    ").append(comp.typeName()).append(" ").append(comp.fieldName());
			if (i < components.size() - 1) {
				sb.append(",");
			}
			sb.append("\n");
		}

		sb.append(") {}\n");

		return sb.toString();
	}

	/**
	 * Represents a single component (field) in the generated record.
	 */
	private record RecordComponent(String typeName, String fieldName) {}
}
