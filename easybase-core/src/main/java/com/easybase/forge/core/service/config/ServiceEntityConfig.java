package com.easybase.forge.core.service.config;

import java.util.ArrayList;
import java.util.List;

public class ServiceEntityConfig {

	private String name;

	private String tablePrefix = "";

	private IdConfig id;

	private AuditConfig audit;

	private SoftDeleteConfig softDelete;

	private TenantConfig tenant;

	private List<ServiceField> fields = new ArrayList<>();

	private List<RelationshipConfig> relationships = new ArrayList<>();

	private CrudOptions crud = new CrudOptions();
	private HookOptions hook = new HookOptions();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTablePrefix() {
		return tablePrefix;
	}

	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	public IdConfig getId() {
		return id;
	}

	public void setId(IdConfig id) {
		this.id = id;
	}

	public AuditConfig getAudit() {
		return audit;
	}

	public void setAudit(AuditConfig audit) {
		this.audit = audit;
	}

	public SoftDeleteConfig getSoftDelete() {
		return softDelete;
	}

	public void setSoftDelete(SoftDeleteConfig softDelete) {
		this.softDelete = softDelete;
	}

	public TenantConfig getTenant() {
		return tenant;
	}

	public void setTenant(TenantConfig tenant) {
		this.tenant = tenant;
	}

	public List<ServiceField> getFields() {
		return fields;
	}

	public void setFields(List<ServiceField> fields) {
		this.fields = fields != null ? fields : new ArrayList<>();
	}

	public List<RelationshipConfig> getRelationships() {
		return relationships;
	}

	public void setRelationships(List<RelationshipConfig> relationships) {
		this.relationships = relationships != null ? relationships : new ArrayList<>();
	}

	public CrudOptions getCrud() {
		return crud;
	}

	public void setCrud(CrudOptions crud) {
		this.crud = crud;
	}

	public HookOptions getHook() {
		return hook;
	}

	public void setHook(HookOptions hook) {
		this.hook = hook;
	}
}
