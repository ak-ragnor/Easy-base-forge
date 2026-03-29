package com.easybase.forge.core.service.generator;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.easybase.forge.core.service.config.ServiceConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

/**
 * Shared utilities for all Service Builder generators.
 *
 * <p>Provides package resolution, type mapping, and naming convention helpers
 * used consistently across every generator.
 */
public final class ServiceGeneratorUtils {

	private ServiceGeneratorUtils() {
		throw new UnsupportedOperationException("Utility class");
	}

	// -------------------------------------------------------------------------
	// Package and path resolution
	// -------------------------------------------------------------------------

	/** Returns the fully-qualified package for domain model classes. */
	public static String modelPackage(ServiceConfig config) {
		return config.getBasePackage() + ".model";
	}

	/** Returns the fully-qualified package for repository interfaces. */
	public static String repositoryPackage(ServiceConfig config) {
		return config.getBasePackage() + ".repository";
	}

	/** Returns the fully-qualified package for persistence classes (JPA entities, adapters). */
	public static String persistencePackage(ServiceConfig config) {
		return config.getBasePackage() + ".persistence";
	}

	/** Returns the fully-qualified package for the base service layer. */
	public static String baseServicePackage(ServiceConfig config) {
		return config.getBasePackage() + ".service.base";
	}

	/** Returns the fully-qualified package for the developer-owned service layer. */
	public static String servicePackage(ServiceConfig config) {
		return config.getBasePackage() + ".service";
	}

	/** Returns the fully-qualified package for hook interfaces and implementations. */
	public static String hookPackage(ServiceConfig config) {
		return config.getBasePackage() + ".hook";
	}

	/** Converts a dot-separated package name to a filesystem {@link Path} under the given base. */
	public static Path packageToPath(Path base, String packageName) {
		return base.resolve(packageName.replace('.', '/'));
	}

	// -------------------------------------------------------------------------
	// Naming conventions
	// -------------------------------------------------------------------------

	/** Returns the entity name in PascalCase (e.g. {@code User}). */
	public static String entityName(ServiceConfig config) {
		return config.getEntity();
	}

	/** Returns the entity name with {@code Entity} suffix (e.g. {@code UserEntity}). */
	public static String entityClassName(ServiceConfig config) {
		return config.getEntity() + "Entity";
	}

	/** Returns the Spring Data JPA repository name (e.g. {@code UserJpaRepository}). */
	public static String jpaRepositoryName(ServiceConfig config) {
		return config.getEntity() + "JpaRepository";
	}

	/** Returns the domain repository interface name (e.g. {@code UserRepository}). */
	public static String repositoryName(ServiceConfig config) {
		return config.getEntity() + "Repository";
	}

	/** Returns the persistence adapter class name (e.g. {@code UserPersistenceAdapter}). */
	public static String persistenceAdapterName(ServiceConfig config) {
		return config.getEntity() + "PersistenceAdapter";
	}

	/** Returns the base service interface name (e.g. {@code UserBaseService}). */
	public static String baseServiceName(ServiceConfig config) {
		return config.getEntity() + "BaseService";
	}

	/** Returns the abstract base service implementation name (e.g. {@code UserBaseServiceImpl}). */
	public static String baseServiceImplName(ServiceConfig config) {
		return config.getEntity() + "BaseServiceImpl";
	}

	/** Returns the developer-owned service interface name (e.g. {@code UserService}). */
	public static String serviceName(ServiceConfig config) {
		return config.getEntity() + "Service";
	}

	/** Returns the developer-owned service implementation name (e.g. {@code UserServiceImpl}). */
	public static String serviceImplName(ServiceConfig config) {
		return config.getEntity() + "ServiceImpl";
	}

	/** Returns the hook interface name (e.g. {@code UserHook}). */
	public static String hookName(ServiceConfig config) {
		return config.getEntity() + "Hook";
	}

	/** Returns the developer-owned hook implementation name (e.g. {@code UserHookImpl}). */
	public static String hookImplName(ServiceConfig config) {
		return config.getEntity() + "HookImpl";
	}

	/**
	 * Derives the JPA table name from the entity name.
	 *
	 * <p>Applies the configured prefix and converts PascalCase to lower_snake_case.
	 * For example: {@code "User"} with prefix {@code "eb_"} → {@code "eb_users"}.
	 */
	public static String tableName(ServiceConfig config) {
		String snake = toSnakeCase(config.getEntity());
		String prefix = config.getTablePrefix();

		if (prefix == null || prefix.isBlank()) {
			return snake + "s";
		}

		return prefix + snake + "s";
	}

	/**
	 * Converts a camelCase or PascalCase name to lower_snake_case.
	 *
	 * <p>For example: {@code "tenantId"} → {@code "tenant_id"},
	 * {@code "UserProfile"} → {@code "user_profile"}.
	 */
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

	/**
	 * Derives a foreign key column name from a relationship field name.
	 *
	 * <p>For example: {@code "tenant"} → {@code "tenant_id"}.
	 */
	public static String deriveForeignKeyColumn(String fieldName) {
		return toSnakeCase(fieldName) + "_id";
	}

	/**
	 * Derives a foreign key constraint name from the table name and column name.
	 *
	 * <p>For example: table {@code "eb_users"}, column {@code "tenant_id"} → {@code "fk_eb_users_tenant_id"}.
	 */
	public static String deriveForeignKeyName(String tableName, String columnName) {
		return "fk_" + tableName + "_" + columnName;
	}

	// -------------------------------------------------------------------------
	// Type resolution
	// -------------------------------------------------------------------------

	/**
	 * Resolves a Java type string to a JavaPoet {@link TypeName}.
	 *
	 * <p>Supports all primitive wrapper types, common JDK types, and
	 * unknown types resolved against the given package.
	 *
	 * @param javaType   the type name (e.g. {@code "UUID"}, {@code "String"})
	 * @param contextPkg the package to use for unknown (domain) types
	 * @return the corresponding JavaPoet {@link TypeName}
	 */
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

	/**
	 * Resolves the primary key {@link TypeName} based on the configured {@code idType}.
	 *
	 * @param config the service configuration
	 * @return the JavaPoet {@link TypeName} for the primary key
	 */
	public static TypeName resolveIdType(ServiceConfig config) {
		return resolveType(config.getIdType(), config.getBasePackage());
	}
}
