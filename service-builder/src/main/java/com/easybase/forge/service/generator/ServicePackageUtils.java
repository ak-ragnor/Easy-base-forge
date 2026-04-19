package com.easybase.forge.service.generator;

import com.easybase.forge.service.config.ServiceConfig;
import com.easybase.forge.service.config.ServiceLayoutResolver;

public final class ServicePackageUtils {

	private ServicePackageUtils() {
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

	private static String resolve(String pattern, ServiceConfig config) {
		return ServiceLayoutResolver.resolve(
				pattern, config.getBasePackage(), config.getModuleName(), config.getLayout());
	}
}
