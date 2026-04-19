package com.easybase.forge.service.config;

import java.util.ArrayList;
import java.util.List;

public class ServiceRootConfig {

	private ServiceStructureConfig structure;

	private List<ServiceModuleConfig> modules = new ArrayList<>();

	public ServiceStructureConfig getStructure() {
		return structure;
	}

	public void setStructure(ServiceStructureConfig structure) {
		this.structure = structure;
	}

	public List<ServiceModuleConfig> getModules() {
		return modules;
	}

	public void setModules(List<ServiceModuleConfig> modules) {
		this.modules = modules != null ? modules : new ArrayList<>();
	}
}
