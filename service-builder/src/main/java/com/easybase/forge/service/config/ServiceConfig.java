package com.easybase.forge.service.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceConfig {

	private String entity;

	private String basePackage;

	private String tablePrefix = "";

	private IdConfig id;

	private AuditConfig audit;

	private SoftDeleteConfig softDelete;

	private TenantConfig tenant;

	private List<ServiceField> fields = new ArrayList<>();

	private List<RelationshipConfig> relationships = new ArrayList<>();

	private ServiceOptions service = new ServiceOptions();
	private CrudOptions crud = new CrudOptions();
	private HookOptions hook = new HookOptions();

	private String moduleName;

	private String layout = ServiceLayoutResolver.MULTI_MODULE;

	private ServiceStructureConfig resolvedStructure = new ServiceStructureConfig();

	private Path resolvedOutputDirectory;

	private ResolvedAuditConfig resolvedAudit;

	private ResolvedSoftDeleteConfig resolvedSoftDelete;

	private ResolvedTenantConfig resolvedTenant;

	private List<String> resolvedAuthors = Collections.emptyList();

	private boolean resolvedAddGeneratedAnnotation = false;

	private boolean resolvedSlf4j = false;

	private String resolvedPostGenerateCommand = null;

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getBasePackage() {
		return basePackage;
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
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

	public String getIdType() {
		if (id != null && id.getType() != null) {
			return id.getType();
		}
		return "UUID";
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
		this.fields = fields;
	}

	public List<RelationshipConfig> getRelationships() {
		return relationships;
	}

	public void setRelationships(List<RelationshipConfig> relationships) {
		this.relationships = relationships;
	}

	public ServiceOptions getService() {
		return service;
	}

	public void setService(ServiceOptions service) {
		this.service = service;
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

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public ServiceStructureConfig getResolvedStructure() {
		return resolvedStructure;
	}

	public void setResolvedStructure(ServiceStructureConfig resolvedStructure) {
		this.resolvedStructure = resolvedStructure;
	}

	public Path getResolvedOutputDirectory() {
		return resolvedOutputDirectory;
	}

	public void setResolvedOutputDirectory(Path resolvedOutputDirectory) {
		this.resolvedOutputDirectory = resolvedOutputDirectory;
	}

	public ResolvedAuditConfig getResolvedAudit() {
		return resolvedAudit;
	}

	public void setResolvedAudit(ResolvedAuditConfig resolvedAudit) {
		this.resolvedAudit = resolvedAudit;
	}

	public ResolvedSoftDeleteConfig getResolvedSoftDelete() {
		return resolvedSoftDelete;
	}

	public void setResolvedSoftDelete(ResolvedSoftDeleteConfig resolvedSoftDelete) {
		this.resolvedSoftDelete = resolvedSoftDelete;
	}

	public ResolvedTenantConfig getResolvedTenant() {
		return resolvedTenant;
	}

	public void setResolvedTenant(ResolvedTenantConfig resolvedTenant) {
		this.resolvedTenant = resolvedTenant;
	}

	public List<String> getResolvedAuthors() {
		return resolvedAuthors;
	}

	public void setResolvedAuthors(List<String> resolvedAuthors) {
		this.resolvedAuthors = resolvedAuthors != null ? resolvedAuthors : Collections.emptyList();
	}

	public boolean isResolvedAddGeneratedAnnotation() {
		return resolvedAddGeneratedAnnotation;
	}

	public void setResolvedAddGeneratedAnnotation(boolean resolvedAddGeneratedAnnotation) {
		this.resolvedAddGeneratedAnnotation = resolvedAddGeneratedAnnotation;
	}

	public boolean isResolvedSlf4j() {
		return resolvedSlf4j;
	}

	public void setResolvedSlf4j(boolean resolvedSlf4j) {
		this.resolvedSlf4j = resolvedSlf4j;
	}

	public String getResolvedPostGenerateCommand() {
		return resolvedPostGenerateCommand;
	}

	public void setResolvedPostGenerateCommand(String resolvedPostGenerateCommand) {
		this.resolvedPostGenerateCommand = resolvedPostGenerateCommand;
	}
}
