package com.easybase.forge.service.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.easybase.forge.common.config.ConfigException;
import com.easybase.forge.common.config.GeneratorConfig;
import com.easybase.forge.common.config.LayoutMode;
import com.easybase.forge.common.config.ServicePackageConfig;
import com.easybase.forge.common.config.StructureConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ServiceConfigLoader {

	private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();

	private ServiceConfigLoader() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static ServiceConfig load(Path entityConfigFile, Path outputDirectory) {
		return load(null, entityConfigFile, outputDirectory);
	}

	public static ServiceConfig load(Path projectConfigFile, Path entityConfigFile, Path outputDirectory) {
		if (!Files.exists(entityConfigFile)) {
			throw new ConfigException("Service config file not found: " + entityConfigFile.toAbsolutePath());
		}

		ServiceConfig config = parseEntityConfig(entityConfigFile);
		GeneratorConfig projectConfig = loadProjectConfigIfPresent(projectConfigFile);

		applyProjectDefaults(config, projectConfig);
		applyResolvedGenerateOptions(config, projectConfig);

		resolveConfig(config);

		if (projectConfig != null) {
			applyStructureOverrides(config.getResolvedStructure(), projectConfig.getStructure());
		}

		config.setResolvedOutputDirectory(outputDirectory);

		validate(config, entityConfigFile);

		return config;
	}

	public static List<ServiceConfig> loadAll(Path serviceYml, Path outputDirectory) {
		return loadAll(null, serviceYml, outputDirectory);
	}

	public static List<ServiceConfig> loadAll(Path projectConfigFile, Path serviceYml, Path outputDirectory) {
		if (!Files.exists(serviceYml)) {
			throw new ConfigException("Service config file not found: " + serviceYml.toAbsolutePath());
		}

		ServiceRootConfig root = parseRootConfig(serviceYml);
		GeneratorConfig projectConfig = loadProjectConfigIfPresent(projectConfigFile);

		String basePackage = projectConfig != null ? projectConfig.getBasePackage() : null;
		String layout = resolveLayoutFromProjectConfig(projectConfig);

		ServiceStructureConfig structure =
				root.getStructure() != null ? mergeStructure(root.getStructure()) : new ServiceStructureConfig();

		if (projectConfig != null) {
			applyStructureOverrides(structure, projectConfig.getStructure());
		}

		List<ServiceConfig> result = new ArrayList<>();

		for (ServiceModuleConfig module : root.getModules()) {
			if (module.getEntities() == null) {
				continue;
			}

			for (ServiceEntityConfig entity : module.getEntities()) {
				ServiceConfig config = toServiceConfig(entity, basePackage, module.getName(), layout, structure);

				applyProjectDefaults(config, projectConfig);
				applyResolvedGenerateOptions(config, projectConfig);
				resolveConfig(config);
				config.setResolvedOutputDirectory(outputDirectory);

				validate(config, serviceYml);
				result.add(config);
			}
		}

		if (result.isEmpty()) {
			throw new ConfigException("No entities found in service config: " + serviceYml.toAbsolutePath());
		}

		return result;
	}

	private static String resolveLayoutFromProjectConfig(GeneratorConfig projectConfig) {
		if (projectConfig == null
				|| projectConfig.getOutput() == null
				|| projectConfig.getOutput().getLayout() == null) {
			return ServiceLayoutResolver.MULTI_MODULE;
		}

		LayoutMode mode = projectConfig.getOutput().getLayout();
		return mode == LayoutMode.FLAT ? ServiceLayoutResolver.FLAT : ServiceLayoutResolver.MULTI_MODULE;
	}

	private static ServiceConfig toServiceConfig(
			ServiceEntityConfig entity,
			String rootBasePackage,
			String moduleName,
			String layout,
			ServiceStructureConfig structure) {
		ServiceConfig config = new ServiceConfig();
		config.setEntity(entity.getName());
		config.setBasePackage(rootBasePackage);
		config.setTablePrefix(entity.getTablePrefix() != null ? entity.getTablePrefix() : "");
		config.setId(entity.getId());
		config.setAudit(entity.getAudit());
		config.setSoftDelete(entity.getSoftDelete());
		config.setTenant(entity.getTenant());
		config.setFields(entity.getFields());
		config.setRelationships(entity.getRelationships());
		config.setCrud(entity.getCrud() != null ? entity.getCrud() : new CrudOptions());
		config.setHook(entity.getHook() != null ? entity.getHook() : new HookOptions());
		config.setModuleName(moduleName);
		config.setLayout(layout);
		config.setResolvedStructure(structure);
		return config;
	}

	private static ServiceStructureConfig mergeStructure(ServiceStructureConfig override) {
		ServiceStructureConfig merged = new ServiceStructureConfig();
		if (override.getModel() != null) merged.setModel(override.getModel());
		if (override.getEntity() != null) merged.setEntity(override.getEntity());
		if (override.getRepositoryBase() != null) merged.setRepositoryBase(override.getRepositoryBase());
		if (override.getRepository() != null) merged.setRepository(override.getRepository());
		if (override.getJpaRepositoryBase() != null) merged.setJpaRepositoryBase(override.getJpaRepositoryBase());
		if (override.getJpaRepository() != null) merged.setJpaRepository(override.getJpaRepository());
		if (override.getPersistenceAdapterBase() != null)
			merged.setPersistenceAdapterBase(override.getPersistenceAdapterBase());
		if (override.getPersistenceAdapter() != null) merged.setPersistenceAdapter(override.getPersistenceAdapter());
		if (override.getHookBase() != null) merged.setHookBase(override.getHookBase());
		if (override.getHook() != null) merged.setHook(override.getHook());
		if (override.getServiceBase() != null) merged.setServiceBase(override.getServiceBase());
		if (override.getService() != null) merged.setService(override.getService());
		return merged;
	}

	private static void applyStructureOverrides(ServiceStructureConfig resolved, StructureConfig projectStructure) {
		if (projectStructure == null) {
			return;
		}

		ServicePackageConfig model = projectStructure.getModel();
		if (model != null && model.getPkg() != null) {
			resolved.setModel(model.getPkg());
		}

		ServicePackageConfig entity = projectStructure.getEntity();
		if (entity != null && entity.getPkg() != null) {
			resolved.setEntity(entity.getPkg());
		}

		ServicePackageConfig repository = projectStructure.getRepository();
		if (repository != null) {
			if (repository.getPkg() != null) {
				resolved.setRepository(repository.getPkg());
			}
			if (repository.getBasePkg() != null) {
				resolved.setRepositoryBase(repository.getBasePkg());
			}
		}

		ServicePackageConfig persistence = projectStructure.getPersistence();
		if (persistence != null) {
			if (persistence.getPkg() != null) {
				resolved.setJpaRepository(persistence.getPkg());
				resolved.setPersistenceAdapter(persistence.getPkg());
			}
			if (persistence.getBasePkg() != null) {
				resolved.setJpaRepositoryBase(persistence.getBasePkg());
				resolved.setPersistenceAdapterBase(persistence.getBasePkg());
			}
		}

		ServicePackageConfig hook = projectStructure.getHook();
		if (hook != null) {
			if (hook.getPkg() != null) {
				resolved.setHook(hook.getPkg());
			}
			if (hook.getBasePkg() != null) {
				resolved.setHookBase(hook.getBasePkg());
			}
		}

		ServicePackageConfig service = projectStructure.getService();
		if (service != null) {
			if (service.getPkg() != null) {
				resolved.setService(service.getPkg());
			}
			if (service.getBasePkg() != null) {
				resolved.setServiceBase(service.getBasePkg());
			}
		}
	}

	private static void resolveConfig(ServiceConfig config) {
		if (config.getModuleName() == null && config.getEntity() != null) {
			config.setModuleName(config.getEntity().toLowerCase());
		}

		if (config.getLayout() == null) {
			config.setLayout(ServiceLayoutResolver.MULTI_MODULE);
		}

		if (config.getResolvedStructure() == null) {
			config.setResolvedStructure(new ServiceStructureConfig());
		}

		config.setResolvedAudit(resolveAuditConfig(config.getAudit()));
		config.setResolvedSoftDelete(resolveSoftDeleteConfig(config.getSoftDelete()));
		config.setResolvedTenant(resolveTenantConfig(config.getTenant()));
	}

	private static ServiceConfig parseEntityConfig(Path entityConfigFile) {
		try (InputStream in = Files.newInputStream(entityConfigFile)) {
			return YAML_MAPPER.readValue(in, ServiceConfig.class);
		} catch (IOException e) {
			throw new ConfigException("Failed to parse service config file: " + entityConfigFile, e);
		}
	}

	private static ServiceRootConfig parseRootConfig(Path serviceYml) {
		try (InputStream in = Files.newInputStream(serviceYml)) {
			return YAML_MAPPER.readValue(in, ServiceRootConfig.class);
		} catch (IOException e) {
			throw new ConfigException("Failed to parse service config file: " + serviceYml, e);
		}
	}

	private static GeneratorConfig loadProjectConfigIfPresent(Path projectConfigFile) {
		if (projectConfigFile == null || !Files.exists(projectConfigFile)) {
			return null;
		}

		try (InputStream in = Files.newInputStream(projectConfigFile)) {
			return YAML_MAPPER.readValue(in, GeneratorConfig.class);
		} catch (IOException e) {
			throw new ConfigException("Failed to parse project config file: " + projectConfigFile, e);
		}
	}

	private static void applyProjectDefaults(ServiceConfig config, GeneratorConfig projectConfig) {
		if (projectConfig == null) {
			return;
		}

		if (config.getBasePackage() == null && projectConfig.getBasePackage() != null) {
			config.setBasePackage(projectConfig.getBasePackage());
		}
	}

	private static void applyResolvedGenerateOptions(ServiceConfig config, GeneratorConfig projectConfig) {
		if (projectConfig == null || projectConfig.getGenerate() == null) {
			return;
		}

		var generate = projectConfig.getGenerate();

		List<String> authors = new ArrayList<>(generate.getAllAuthors());
		config.setResolvedAuthors(Collections.unmodifiableList(authors));
		config.setResolvedAddGeneratedAnnotation(generate.isAddGeneratedAnnotation());
		config.setResolvedSlf4j(generate.isSlf4j());
		config.setResolvedPostGenerateCommand(generate.getPostGenerateCommand());
	}

	private static ResolvedAuditConfig resolveAuditConfig(AuditConfig audit) {
		if (audit == null || Boolean.FALSE.equals(audit.getEnabled())) {
			return new ResolvedAuditConfig(false, "UUID");
		}

		String auditorType = audit.getAuditorType() != null ? audit.getAuditorType() : "UUID";
		return new ResolvedAuditConfig(true, auditorType);
	}

	private static ResolvedSoftDeleteConfig resolveSoftDeleteConfig(SoftDeleteConfig softDelete) {
		if (softDelete == null || Boolean.FALSE.equals(softDelete.getEnabled())) {
			return new ResolvedSoftDeleteConfig(false);
		}

		return new ResolvedSoftDeleteConfig(Boolean.TRUE.equals(softDelete.getEnabled()));
	}

	private static ResolvedTenantConfig resolveTenantConfig(TenantConfig tenant) {
		if (tenant == null || Boolean.FALSE.equals(tenant.getEnabled())) {
			return new ResolvedTenantConfig(false, "UUID");
		}

		String tenantIdType = tenant.getTenantIdType() != null ? tenant.getTenantIdType() : "UUID";
		return new ResolvedTenantConfig(Boolean.TRUE.equals(tenant.getEnabled()), tenantIdType);
	}

	private static void validate(ServiceConfig config, Path configFile) {
		if (config.getEntity() == null || config.getEntity().isBlank()) {
			throw new ConfigException("'entity' (or 'name' in multi-entity format) is required in " + configFile);
		}

		if (config.getBasePackage() == null || config.getBasePackage().isBlank()) {
			throw new ConfigException(
					"'basePackage' is required. Set it in " + configFile + " or in easybase-config.yaml");
		}

		if (config.getResolvedOutputDirectory() == null) {
			throw new ConfigException("Output directory must be provided");
		}
	}
}
