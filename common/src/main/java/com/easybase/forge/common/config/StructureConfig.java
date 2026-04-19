package com.easybase.forge.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps the {@code structure} block in {@code easybase-config.yaml}.
 *
 * <p>Contains package patterns for both REST and service layers so that a single
 * project config file can configure both builders.
 */
public class StructureConfig {

	private ControllerStructureConfig controller = new ControllerStructureConfig();

	@JsonProperty("delegate")
	private PackageConfig delegate = new PackageConfig("{basePackage}.{module}.delegate");

	@JsonProperty("dto")
	private PackageConfig dto = new PackageConfig("{basePackage}.{module}.dto");

	private ServicePackageConfig model;
	private ServicePackageConfig entity;
	private ServicePackageConfig repository;
	private ServicePackageConfig persistence;
	private ServicePackageConfig hook;
	private ServicePackageConfig service;

	public ControllerStructureConfig getController() {
		return controller;
	}

	public void setController(ControllerStructureConfig controller) {
		this.controller = controller;
	}

	public PackageConfig getDelegate() {
		return delegate;
	}

	public void setDelegate(PackageConfig delegate) {
		this.delegate = delegate;
	}

	public PackageConfig getDto() {
		return dto;
	}

	public void setDto(PackageConfig dto) {
		this.dto = dto;
	}

	public ServicePackageConfig getModel() {
		return model;
	}

	public void setModel(ServicePackageConfig model) {
		this.model = model;
	}

	public ServicePackageConfig getEntity() {
		return entity;
	}

	public void setEntity(ServicePackageConfig entity) {
		this.entity = entity;
	}

	public ServicePackageConfig getRepository() {
		return repository;
	}

	public void setRepository(ServicePackageConfig repository) {
		this.repository = repository;
	}

	public ServicePackageConfig getPersistence() {
		return persistence;
	}

	public void setPersistence(ServicePackageConfig persistence) {
		this.persistence = persistence;
	}

	public ServicePackageConfig getHook() {
		return hook;
	}

	public void setHook(ServicePackageConfig hook) {
		this.hook = hook;
	}

	public ServicePackageConfig getService() {
		return service;
	}

	public void setService(ServicePackageConfig service) {
		this.service = service;
	}

	/** Simple package-pattern holder used for delegate and DTO layers. */
	public static class PackageConfig {

		@JsonProperty("package")
		private String pkg;

		public PackageConfig() {}

		public PackageConfig(String pkg) {
			this.pkg = pkg;
		}

		public String getPkg() {
			return pkg;
		}

		public void setPkg(String pkg) {
			this.pkg = pkg;
		}
	}
}
