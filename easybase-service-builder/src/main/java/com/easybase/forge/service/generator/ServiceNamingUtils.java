package com.easybase.forge.service.generator;

import com.easybase.forge.service.config.ServiceConfig;

public final class ServiceNamingUtils {

	private ServiceNamingUtils() {
		throw new UnsupportedOperationException("Utility class");
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

	public static String fieldName(ServiceConfig config, String suffix) {
		String name = config.getEntity();
		return Character.toLowerCase(name.charAt(0)) + name.substring(1) + suffix;
	}
}
