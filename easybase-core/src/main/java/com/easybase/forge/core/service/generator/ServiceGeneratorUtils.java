package com.easybase.forge.core.service.generator;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.easybase.forge.core.service.config.ServiceConfig;
import com.easybase.forge.core.service.config.ServiceLayoutResolver;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public final class ServiceGeneratorUtils {

	private ServiceGeneratorUtils() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static String domainModelPackage(ServiceConfig config) {
		return resolve(config.getResolvedStructure().getModel(), config);
	}

	public static String domainEntityPackage(ServiceConfig config) {
		return resolve(config.getResolvedStructure().getEntity(), config);
	}

	public static String repositoryBasePackage(ServiceConfig config) {
		return resolve(config.getResolvedStructure().getRepositoryBase(), config);
	}

	public static String repositoryPackage(ServiceConfig config) {
		return resolve(config.getResolvedStructure().getRepository(), config);
	}

	public static String jpaRepositoryBasePackage(ServiceConfig config) {
		return resolve(config.getResolvedStructure().getJpaRepositoryBase(), config);
	}

	public static String jpaRepositoryPackage(ServiceConfig config) {
		return resolve(config.getResolvedStructure().getJpaRepository(), config);
	}

	public static String persistenceBasePackage(ServiceConfig config) {
		return resolve(config.getResolvedStructure().getPersistenceAdapterBase(), config);
	}

	public static String persistencePackage(ServiceConfig config) {
		return resolve(config.getResolvedStructure().getPersistenceAdapter(), config);
	}

	public static String hookBasePackage(ServiceConfig config) {
		return resolve(config.getResolvedStructure().getHookBase(), config);
	}

	public static String hookPackage(ServiceConfig config) {
		return resolve(config.getResolvedStructure().getHook(), config);
	}

	public static String serviceBasePackage(ServiceConfig config) {
		return resolve(config.getResolvedStructure().getServiceBase(), config);
	}

	public static String servicePackage(ServiceConfig config) {
		return resolve(config.getResolvedStructure().getService(), config);
	}

	public static String entityName(ServiceConfig config) {
		return config.getEntity();
	}

	public static String entityClassName(ServiceConfig config) {
		return config.getEntity() + "Entity";
	}

	public static String repositoryBaseName(ServiceConfig config) {
		return config.getEntity() + "RepositoryBase";
	}

	public static String repositoryName(ServiceConfig config) {
		return config.getEntity() + "Repository";
	}

	public static String jpaRepositoryBaseName(ServiceConfig config) {
		return config.getEntity() + "JpaRepositoryBase";
	}

	public static String jpaRepositoryName(ServiceConfig config) {
		return config.getEntity() + "JpaRepository";
	}

	public static String persistenceAdapterBaseName(ServiceConfig config) {
		return config.getEntity() + "PersistenceAdapterBase";
	}

	public static String persistenceAdapterName(ServiceConfig config) {
		return config.getEntity() + "PersistenceAdapter";
	}

	public static String hookBaseName(ServiceConfig config) {
		return config.getEntity() + "HookBase";
	}

	public static String hookName(ServiceConfig config) {
		return config.getEntity() + "Hook";
	}

	public static String serviceBaseName(ServiceConfig config) {
		return config.getEntity() + "ServiceBase";
	}

	public static String serviceBaseImplName(ServiceConfig config) {
		return config.getEntity() + "ServiceBaseImpl";
	}

	public static String localServiceBaseName(ServiceConfig config) {
		return config.getEntity() + "LocalServiceBase";
	}

	public static String localServiceBaseImplName(ServiceConfig config) {
		return config.getEntity() + "LocalServiceBaseImpl";
	}

	public static String localServiceName(ServiceConfig config) {
		return config.getEntity() + "LocalService";
	}

	public static String serviceName(ServiceConfig config) {
		return config.getEntity() + "Service";
	}

	public static String tableName(ServiceConfig config) {
		String snake = toSnakeCase(config.getEntity());
		String prefix = config.getTablePrefix();

		if (prefix == null || prefix.isBlank()) {
			return snake + "s";
		}

		return prefix + snake + "s";
	}

	public static String toSnakeCase(String name) {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);

			if (Character.isUpperCase(c) && i > 0) {
				result.append('_');
			}

			result.append(Character.toLowerCase(c));
		}

		return result.toString();
	}

	public static String deriveForeignKeyColumn(String fieldName) {
		return toSnakeCase(fieldName) + "_id";
	}

	public static String deriveForeignKeyName(String tableName, String columnName) {
		return "fk_" + tableName + "_" + columnName;
	}

	public static Path packageToPath(Path base, String packageName) {
		return base.resolve(packageName.replace('.', '/'));
	}

	public static TypeName resolveType(String javaType, String contextPkg) {
		if (javaType == null || javaType.isBlank()) {
			return TypeName.VOID;
		}

		if ("String".equals(javaType)) {
			return ClassName.get(String.class);
		}

		if ("Integer".equals(javaType)) {
			return ClassName.get(Integer.class);
		}

		if ("Long".equals(javaType)) {
			return ClassName.get(Long.class);
		}

		if ("Boolean".equals(javaType)) {
			return ClassName.get(Boolean.class);
		}

		if ("BigDecimal".equals(javaType)) {
			return ClassName.get(BigDecimal.class);
		}

		if ("LocalDate".equals(javaType)) {
			return ClassName.get(LocalDate.class);
		}

		if ("OffsetDateTime".equals(javaType)) {
			return ClassName.get(OffsetDateTime.class);
		}

		if ("UUID".equals(javaType)) {
			return ClassName.get(UUID.class);
		}

		if ("Instant".equals(javaType)) {
			return ClassName.get(Instant.class);
		}

		return ClassName.get(contextPkg, javaType);
	}

	public static TypeName resolveIdType(ServiceConfig config) {
		return resolveType(config.getIdType(), config.getBasePackage());
	}

	public static String fieldName(ServiceConfig config, String suffix) {
		String name = config.getEntity();
		return Character.toLowerCase(name.charAt(0)) + name.substring(1) + suffix;
	}

	public static void applyAuthors(TypeSpec.Builder builder, ServiceConfig config) {
		List<String> authors = config.getResolvedAuthors();
		if (authors == null || authors.isEmpty()) {
			return;
		}
		StringBuilder javadoc = new StringBuilder();
		for (String author : authors) {
			javadoc.append("\n@author ").append(author);
		}
		builder.addJavadoc(javadoc.toString());
	}

	private static String resolve(String pattern, ServiceConfig config) {
		return ServiceLayoutResolver.resolve(
				pattern, config.getBasePackage(), config.getModuleName(), config.getLayout());
	}
}
