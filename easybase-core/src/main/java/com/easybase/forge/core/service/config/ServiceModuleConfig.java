package com.easybase.forge.core.service.config;

import java.util.ArrayList;
import java.util.List;

public class ServiceModuleConfig {

	private String name;

	private List<ServiceEntityConfig> entities = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ServiceEntityConfig> getEntities() {
		return entities;
	}

	public void setEntities(List<ServiceEntityConfig> entities) {
		this.entities = entities != null ? entities : new ArrayList<>();
	}
}
