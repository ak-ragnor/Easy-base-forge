package com.easybase.forge.core.service.generator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.model.ArtifactType;
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
 * Generates the persistence adapter that bridges the domain repository interface
 * and Spring Data JPA.
 *
 * <p>The adapter implements {@code UserRepository} and delegates all persistence
 * operations to {@code UserJpaRepository}. It contains two private helper methods:
 * <ul>
 *   <li>{@code toDomain(UserEntity)} — maps from the JPA entity to the domain record.</li>
 *   <li>{@code toEntity(User)} — maps from the domain record to the JPA entity.</li>
 * </ul>
 *
 * <p>When {@code audit.softDelete = true}:
 * <ul>
 *   <li>{@code findById} delegates to {@code jpaRepository.findActiveById}.</li>
 *   <li>{@code findAll} delegates to {@code jpaRepository.findAllActive}.</li>
 *   <li>{@code deleteById} sets the {@code deleted} flag to {@code true} instead of
 *       hard-deleting the record.</li>
 * </ul>
 *
 * <p>For {@code MANY_TO_ONE} and {@code ONE_TO_ONE} relationships, the {@code toDomain}
 * method extracts only the foreign key value (e.g. {@code entity.getTenant().getId()}).
 * The {@code toEntity} method sets only basic fields — callers are responsible for
 * loading and injecting related entities.
 */
public class PersistenceAdapterGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.persistencePackage(config);
		String adapterName = ServiceGeneratorUtils.persistenceAdapterName(config);
		String repoName = ServiceGeneratorUtils.repositoryName(config);
		String jpaRepoName = ServiceGeneratorUtils.jpaRepositoryName(config);
		String entityName = ServiceGeneratorUtils.entityName(config);
		String repoPkg = ServiceGeneratorUtils.repositoryPackage(config);
		String modelPkg = ServiceGeneratorUtils.modelPackage(config);

		ClassName domainType = ClassName.get(modelPkg, entityName);
		ClassName entityType = ClassName.get(pkg, entityName + "Entity");
		ClassName repoInterface = ClassName.get(repoPkg, repoName);
		ClassName jpaRepoType = ClassName.get(pkg, jpaRepoName);

		TypeSpec adapter = TypeSpec.classBuilder(adapterName)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(repoInterface)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("org.springframework.stereotype", "Component"))
						.build())
				.addAnnotation(AnnotationSpec.builder(ClassName.get("lombok", "RequiredArgsConstructor"))
						.build())
				.addJavadoc(
						"Persistence adapter for {@code $L}.\n\n"
								+ "<p>Bridges {@link $T} and Spring Data JPA. "
								+ "This class is always regenerated — do not edit it.\n"
								+ "Add custom query methods to {@code $LJpaRepository} instead.\n",
						entityName,
						repoInterface,
						entityName)
				.addField(buildJpaRepoField(jpaRepoType, jpaRepoName))
				.addMethod(buildSaveMethod(config, domainType, entityType))
				.addMethod(buildFindByIdMethod(config, domainType, entityType))
				.addMethod(buildFindAllMethod(config, domainType, entityType))
				.addMethod(buildDeleteByIdMethod(config, entityType))
				.addMethod(buildExistsByIdMethod(config))
				.addMethod(buildToDomainMethod(config, domainType, entityType))
				.addMethod(buildToEntityMethod(config, domainType, entityType))
				.build();

		JavaFile javaFile =
				JavaFile.builder(pkg, adapter).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(adapterName + ".java");

		return List.of(
				new GeneratedArtifact(outputPath, ArtifactType.SERVICE_PERSISTENCE_ADAPTER, javaFile.toString()));
	}

	private FieldSpec buildJpaRepoField(ClassName jpaRepoType, String jpaRepoName) {
		String fieldName = Character.toLowerCase(jpaRepoName.charAt(0)) + jpaRepoName.substring(1);

		return FieldSpec.builder(jpaRepoType, fieldName, Modifier.PRIVATE, Modifier.FINAL)
				.build();
	}

	private MethodSpec buildSaveMethod(ServiceConfig config, ClassName domainType, ClassName entityType) {
		String jpaRepoFieldName = buildJpaRepoFieldName(config);

		return MethodSpec.methodBuilder("save")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(domainType, "entity")
				.returns(domainType)
				.addStatement("$T jpaEntity = toEntity(entity)", entityType)
				.addStatement("$T saved = $L.save(jpaEntity)", entityType, jpaRepoFieldName)
				.addStatement("return toDomain(saved)")
				.build();
	}

	private MethodSpec buildFindByIdMethod(ServiceConfig config, ClassName domainType, ClassName entityType) {
		TypeName idType = ServiceGeneratorUtils.resolveIdType(config);
		ParameterizedTypeName returnType = ParameterizedTypeName.get(ClassName.get(Optional.class), domainType);
		String jpaRepoFieldName = buildJpaRepoFieldName(config);
		ResolvedAuditConfig audit = config.getResolvedAudit();

		MethodSpec.Builder builder = MethodSpec.methodBuilder("findById")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(idType, "id")
				.returns(returnType);

		if (audit.softDelete()) {
			builder.addStatement("return $L.findActiveById(id).map(this::toDomain)", jpaRepoFieldName);
		} else {
			builder.addStatement("return $L.findById(id).map(this::toDomain)", jpaRepoFieldName);
		}

		return builder.build();
	}

	private MethodSpec buildFindAllMethod(ServiceConfig config, ClassName domainType, ClassName entityType) {
		ParameterizedTypeName returnType = ParameterizedTypeName.get(ClassName.get(List.class), domainType);
		String jpaRepoFieldName = buildJpaRepoFieldName(config);
		ResolvedAuditConfig audit = config.getResolvedAudit();

		MethodSpec.Builder builder = MethodSpec.methodBuilder("findAll")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(returnType);

		ParameterizedTypeName entityListType = ParameterizedTypeName.get(ClassName.get(List.class), entityType);

		if (audit.softDelete()) {
			builder.addStatement("$T entities = $L.findAllActive()", entityListType, jpaRepoFieldName);
		} else {
			builder.addStatement("$T entities = $L.findAll()", entityListType, jpaRepoFieldName);
		}

		builder.addStatement("$T result = new $T<>()", returnType, ClassName.get(ArrayList.class));
		builder.beginControlFlow("for ($T e : entities)", entityType);
		builder.addStatement("result.add(toDomain(e))");
		builder.endControlFlow();
		builder.addStatement("return result");

		return builder.build();
	}

	private MethodSpec buildDeleteByIdMethod(ServiceConfig config, ClassName entityType) {
		TypeName idType = ServiceGeneratorUtils.resolveIdType(config);
		String jpaRepoFieldName = buildJpaRepoFieldName(config);
		ResolvedAuditConfig audit = config.getResolvedAudit();

		MethodSpec.Builder builder = MethodSpec.methodBuilder("deleteById")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(idType, "id");

		if (audit.softDelete()) {
			builder.addStatement("$T entity = $L.findById(id).orElse(null)", entityType, jpaRepoFieldName);
			builder.beginControlFlow("if (entity != null)");
			builder.addStatement("entity.set$L(true)", capitalise(audit.softDeleteColumn()));
			builder.addStatement("$L.save(entity)", jpaRepoFieldName);
			builder.endControlFlow();
		} else {
			builder.addStatement("$L.deleteById(id)", jpaRepoFieldName);
		}

		return builder.build();
	}

	private MethodSpec buildExistsByIdMethod(ServiceConfig config) {
		TypeName idType = ServiceGeneratorUtils.resolveIdType(config);
		String jpaRepoFieldName = buildJpaRepoFieldName(config);

		return MethodSpec.methodBuilder("existsById")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(idType, "id")
				.returns(boolean.class)
				.addStatement("return $L.existsById(id)", jpaRepoFieldName)
				.build();
	}

	private MethodSpec buildToDomainMethod(ServiceConfig config, ClassName domainType, ClassName entityType) {
		MethodSpec.Builder builder = MethodSpec.methodBuilder("toDomain")
				.addModifiers(Modifier.PRIVATE)
				.addParameter(entityType, "entity")
				.returns(domainType);

		List<String> args = buildToDomainArgs(config);
		String constructorArgs = String.join(",\n        ", args);

		builder.addStatement("return new $T(\n        $L)", domainType, constructorArgs);

		return builder.build();
	}

	private List<String> buildToDomainArgs(ServiceConfig config) {
		ResolvedAuditConfig audit = config.getResolvedAudit();
		List<String> args = new ArrayList<>();

		args.add("entity.getId()");

		for (ServiceField field : config.getFields()) {
			args.add("entity.get" + capitalise(field.getName()) + "()");
		}

		for (RelationshipConfig rel : config.getRelationships()) {
			if (rel.getType() == RelationType.MANY_TO_ONE || rel.getType() == RelationType.ONE_TO_ONE) {
				String getter = "entity.get" + capitalise(rel.getField()) + "()";
				args.add(getter + " != null ? " + getter + ".getId() : null");
			}
		}

		if (audit.enabled()) {
			args.add("entity.getCreatedAt()");
			args.add("entity.getCreatedBy()");
			args.add("entity.getUpdatedAt()");
			args.add("entity.getUpdatedBy()");

			if (audit.softDelete()) {
				args.add("entity.get" + capitalise(audit.softDeleteColumn()) + "()");
			}
		}

		return args;
	}

	private MethodSpec buildToEntityMethod(ServiceConfig config, ClassName domainType, ClassName entityType) {
		MethodSpec.Builder builder = MethodSpec.methodBuilder("toEntity")
				.addModifiers(Modifier.PRIVATE)
				.addParameter(domainType, "domain")
				.returns(entityType)
				.addStatement("$T entity = new $T()", entityType, entityType);

		for (ServiceField field : config.getFields()) {
			String cap = capitalise(field.getName());
			builder.addStatement("entity.set$L(domain.$L())", cap, field.getName());
		}

		builder.addJavadoc("Note: related entities ({@code MANY_TO_ONE}, {@code ONE_TO_ONE}) are not mapped here.\n"
				+ "Load them by ID and set them on the entity in your service implementation.\n");

		builder.addStatement("return entity");

		return builder.build();
	}

	private String buildJpaRepoFieldName(ServiceConfig config) {
		String jpaRepoName = ServiceGeneratorUtils.jpaRepositoryName(config);

		return Character.toLowerCase(jpaRepoName.charAt(0)) + jpaRepoName.substring(1);
	}

	private String capitalise(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}

		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}
}
