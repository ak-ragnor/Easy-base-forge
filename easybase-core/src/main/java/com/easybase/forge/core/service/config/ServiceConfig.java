package com.easybase.forge.core.service.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Root configuration object parsed from a per-entity {@code easybase.yml}.
 *
 * <p>Project-level defaults (audit, basePackage) are read from {@code easybase-config.yaml}
 * and merged into this object by {@link ServiceConfigLoader} before the generators run.
 * After loading, {@link #getResolvedAudit()} always returns a fully-resolved non-null view.
 *
 * <p>Example {@code easybase.yml}:
 * <pre>
 * entity: User
 * tablePrefix: eb_
 * idType: UUID
 *
 * fields:
 *   - name: email
 *     type: String
 *     required: true
 *     maxLength: 255
 *     unique: true
 *
 * relationships:
 *   - type: MANY_TO_ONE
 *     entity: Tenant
 *     field: tenant
 *     nullable: false
 *     fetch: LAZY
 *     foreignKey: fk_user_tenant
 *
 * audit:
 *   softDelete: false
 *
 * service:
 *   type: local
 *   generateCrud: true
 *
 * crud:
 *   create: true
 *   update: true
 *   delete: true
 *   get: true
 *   list: true
 *
 * hook:
 *   enabled: true
 *   multiple: true
 * </pre>
 */
public class ServiceConfig {

	/** Entity class name in PascalCase (e.g. {@code User}). Required. */
	private String entity;

	/**
	 * Base Java package for generated classes (e.g. {@code com.example.app}).
	 * Inherited from {@code easybase-config.yaml} when not set here.
	 */
	private String basePackage;

	/** Optional table name prefix (e.g. {@code eb_} → {@code eb_users}). Default: empty. */
	private String tablePrefix = "";

	/**
	 * Java type for the primary key field.
	 * Accepted values: {@code UUID}, {@code Long}, {@code String}, {@code Integer}.
	 * Default: {@code UUID}.
	 */
	private String idType = "UUID";

	/** Entity fields. */
	private List<ServiceField> fields = new ArrayList<>();

	/** JPA relationships. */
	private List<RelationshipConfig> relationships = new ArrayList<>();

	/**
	 * Per-entity audit overrides. {@code null} means "use project defaults entirely".
	 * Individual fields within this object may also be {@code null} (inherit).
	 */
	private AuditConfig audit;

	private ServiceOptions service = new ServiceOptions();
	private CrudOptions crud = new CrudOptions();
	private HookOptions hook = new HookOptions();

	/** Resolved output directory — set by {@link ServiceConfigLoader} after loading. */
	private Path resolvedOutputDirectory;

	/**
	 * Fully-resolved audit config after merging project defaults with entity overrides.
	 * Set by {@link ServiceConfigLoader}; never {@code null} after loading.
	 */
	private ResolvedAuditConfig resolvedAudit;

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

	public String getIdType() {
		return idType;
	}

	public void setIdType(String idType) {
		this.idType = idType;
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

	public AuditConfig getAudit() {
		return audit;
	}

	public void setAudit(AuditConfig audit) {
		this.audit = audit;
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
}
