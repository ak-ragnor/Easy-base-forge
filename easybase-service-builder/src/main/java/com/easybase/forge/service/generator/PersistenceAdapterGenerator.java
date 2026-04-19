package com.easybase.forge.service.generator;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.util.PackageUtils;
import com.easybase.forge.service.config.RelationType;
import com.easybase.forge.service.config.RelationshipConfig;
import com.easybase.forge.service.config.ServiceConfig;
import com.easybase.forge.service.config.ServiceField;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class PersistenceAdapterGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServicePackageUtils.persistencePackage(config);
		String adapterName = ServiceNamingUtils.persistenceAdapterName(config);
		String adapterBasePkg = ServicePackageUtils.persistenceBasePackage(config);
		String adapterBaseName = ServiceNamingUtils.persistenceAdapterBaseName(config);
		String entityName = ServiceNamingUtils.entityName(config);
		String modelPkg = ServicePackageUtils.domainModelPackage(config);
		String entityPkg = ServicePackageUtils.domainEntityPackage(config);
		String jpaRepoPkg = ServicePackageUtils.jpaRepositoryPackage(config);
		String jpaRepoName = ServiceNamingUtils.jpaRepositoryName(config);

		ClassName domainType = ClassName.get(modelPkg, entityName);
		ClassName entityType = ClassName.get(entityPkg, entityName + "Entity");
		ClassName adapterBaseType = ClassName.get(adapterBasePkg, adapterBaseName);
		ClassName jpaRepoType = ClassName.get(jpaRepoPkg, jpaRepoName);

		TypeSpec.Builder builder = TypeSpec.classBuilder(adapterName)
				.addModifiers(Modifier.PUBLIC)
				.superclass(adapterBaseType)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("org.springframework.stereotype", "Component"))
						.build())
				.addJavadoc(
						"Developer-owned persistence adapter for {@code $L}.\n\n"
								+ "<p>Extend or override methods from {@code $LPersistenceAdapterBase} here.\n"
								+ "Add custom persistence logic as needed.\n\n"
								+ "<p>This file is generated once and never overwritten.\n",
						entityName,
						entityName)
				.addMethod(buildConstructor(config, jpaRepoType))
				.addMethod(buildToDomainMethod(config, domainType, entityType))
				.addMethod(buildToEntityMethod(config, domainType, entityType));

		ServiceMetaUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), pkg)
				.resolve(adapterName + ".java");

		return List.of(new GeneratedArtifact(
				outputPath, ServiceArtifactType.SERVICE_PERSISTENCE_ADAPTER, javaFile.toString()));
	}

	private MethodSpec buildConstructor(ServiceConfig config, ClassName jpaRepoType) {
		String jpaRepoParamName = ServiceNamingUtils.fieldName(config, "JpaRepository");

		MethodSpec.Builder builder = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(jpaRepoType, jpaRepoParamName);

		if (config.getHook().isEnabled()) {
			String hookBasePkg = ServicePackageUtils.hookBasePackage(config);
			String hookBaseName = ServiceNamingUtils.hookBaseName(config);
			ClassName hookBaseType = ClassName.get(hookBasePkg, hookBaseName);
			ParameterizedTypeName hookListType =
					ParameterizedTypeName.get(ClassName.get(java.util.List.class), hookBaseType);

			String hooksParamName = ServiceNamingUtils.fieldName(config, "Hooks");
			builder.addParameter(hookListType, hooksParamName);
			builder.addStatement("super($L, $L)", jpaRepoParamName, hooksParamName);
		} else {
			builder.addStatement("super($L)", jpaRepoParamName);
		}

		return builder.build();
	}

	private MethodSpec buildToDomainMethod(ServiceConfig config, ClassName domainType, ClassName entityType) {
		MethodSpec.Builder builder = MethodSpec.methodBuilder("toDomain")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PROTECTED)
				.addParameter(entityType, "entity")
				.returns(domainType);

		builder.addStatement("$T domain = new $T()", domainType, domainType);
		builder.addCode("\n");
		builder.addStatement("domain.setId(entity.getId())");

		for (ServiceField field : config.getFields()) {
			String name = capitalise(field.getName());
			builder.addStatement("domain.set$L(entity.get$L())", name, name);
		}

		for (RelationshipConfig rel : config.getRelationships()) {
			if (rel.getType() == RelationType.ONE_TO_ONE) {
				String fkField = snakeToCamelCase(rel.getColumn());
				String setterName = capitalise(fkField);
				builder.addStatement("domain.set$L(entity.get$L())", setterName, setterName);
			}
		}

		if (config.getResolvedAudit().enabled()) {
			builder.addCode("\n");
			builder.addStatement("domain.setCreatedAt(entity.getCreatedAt())");
			builder.addStatement("domain.setCreatedBy(entity.getCreatedBy())");
			builder.addStatement("domain.setUpdatedAt(entity.getUpdatedAt())");
			builder.addStatement("domain.setUpdatedBy(entity.getUpdatedBy())");
		}

		if (config.getResolvedSoftDelete().enabled()) {
			builder.addStatement("domain.setDeleted(entity.getDeleted())");
		}

		if (config.getResolvedTenant().enabled()) {
			builder.addStatement("domain.setTenantId(entity.getTenantId())");
		}

		builder.addCode("\n");
		builder.addStatement("return domain");

		return builder.build();
	}

	private MethodSpec buildToEntityMethod(ServiceConfig config, ClassName domainType, ClassName entityType) {
		MethodSpec.Builder builder = MethodSpec.methodBuilder("toEntity")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PROTECTED)
				.addParameter(domainType, "domain")
				.returns(entityType)
				.addJavadoc("Maps the domain model to a JPA entity.\n"
						+ "Note: {@code id} is not set — UUID entities self-generate their id.\n"
						+ "Audit timestamps are managed by Hibernate "
						+ "{@code @CreationTimestamp}/{@code @UpdateTimestamp}.\n");

		builder.addStatement("$T entity = new $T()", entityType, entityType);
		builder.addCode("\n");

		for (ServiceField field : config.getFields()) {
			String name = capitalise(field.getName());
			builder.addStatement("entity.set$L(domain.get$L())", name, name);
		}

		for (RelationshipConfig rel : config.getRelationships()) {
			if (rel.getType() == RelationType.ONE_TO_ONE) {
				String fkField = snakeToCamelCase(rel.getColumn());
				String setterName = capitalise(fkField);
				builder.addStatement("entity.set$L(domain.get$L())", setterName, setterName);
			}
		}

		builder.addCode("\n");
		builder.addStatement("return entity");

		return builder.build();
	}

	private String capitalise(String name) {
		if (name == null || name.isEmpty()) return name;
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
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
