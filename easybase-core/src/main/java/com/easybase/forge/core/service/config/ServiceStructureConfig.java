package com.easybase.forge.core.service.config;

public class ServiceStructureConfig {

	private String model = "{basePackage}.{module}.domain.model";
	private String entity = "{basePackage}.{module}.domain.entity";
	private String repositoryBase = "{basePackage}.{module}.infrastructure.repository.base";
	private String repository = "{basePackage}.{module}.infrastructure.repository";
	private String jpaRepositoryBase = "{basePackage}.{module}.infrastructure.persistence.base";
	private String jpaRepository = "{basePackage}.{module}.infrastructure.persistence";
	private String persistenceAdapterBase = "{basePackage}.{module}.infrastructure.persistence.base";
	private String persistenceAdapter = "{basePackage}.{module}.infrastructure.persistence";
	private String hookBase = "{basePackage}.{module}.infrastructure.hook.base";
	private String hook = "{basePackage}.{module}.infrastructure.hook";
	private String serviceBase = "{basePackage}.{module}.service.base";
	private String service = "{basePackage}.{module}.service";

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getRepositoryBase() {
		return repositoryBase;
	}

	public void setRepositoryBase(String repositoryBase) {
		this.repositoryBase = repositoryBase;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getJpaRepositoryBase() {
		return jpaRepositoryBase;
	}

	public void setJpaRepositoryBase(String jpaRepositoryBase) {
		this.jpaRepositoryBase = jpaRepositoryBase;
	}

	public String getJpaRepository() {
		return jpaRepository;
	}

	public void setJpaRepository(String jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	public String getPersistenceAdapterBase() {
		return persistenceAdapterBase;
	}

	public void setPersistenceAdapterBase(String persistenceAdapterBase) {
		this.persistenceAdapterBase = persistenceAdapterBase;
	}

	public String getPersistenceAdapter() {
		return persistenceAdapter;
	}

	public void setPersistenceAdapter(String persistenceAdapter) {
		this.persistenceAdapter = persistenceAdapter;
	}

	public String getHookBase() {
		return hookBase;
	}

	public void setHookBase(String hookBase) {
		this.hookBase = hookBase;
	}

	public String getHook() {
		return hook;
	}

	public void setHook(String hook) {
		this.hook = hook;
	}

	public String getServiceBase() {
		return serviceBase;
	}

	public void setServiceBase(String serviceBase) {
		this.serviceBase = serviceBase;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}
}
