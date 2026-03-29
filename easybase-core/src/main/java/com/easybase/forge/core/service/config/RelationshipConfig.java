package com.easybase.forge.core.service.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes one JPA relationship on a generated entity.
 *
 * <p>Example YAML:
 * <pre>
 * relationships:
 *   - type: MANY_TO_ONE
 *     entity: Tenant
 *     field: tenant
 *     column: tenant_id
 *     nullable: false
 *     fetch: LAZY
 *     cascade: []
 *     foreignKey: fk_user_tenant
 *   - type: ONE_TO_MANY
 *     entity: UserCredential
 *     field: credentials
 *     mappedBy: user
 *     cascade: [ALL]
 *     fetch: LAZY
 * </pre>
 */
public class RelationshipConfig {

	/** Relationship type: {@code MANY_TO_ONE}, {@code ONE_TO_MANY}, or {@code ONE_TO_ONE}. */
	private RelationType type;

	/** Simple class name of the related entity (e.g. {@code Tenant}). */
	private String entity;

	/** Java field name on this entity's JPA class (e.g. {@code tenant}). */
	private String field;

	/**
	 * Database column name for the foreign key (owning side).
	 * Auto-derived as {@code snake_case(field) + "_id"} when not set.
	 * Not applicable for {@code ONE_TO_MANY} (non-owning side).
	 */
	private String column;

	/** Whether the foreign key column allows null. Default: {@code true}. */
	private boolean nullable = true;

	/** JPA fetch strategy. Default: {@link FetchStrategy#LAZY}. */
	private FetchStrategy fetch = FetchStrategy.LAZY;

	/** Cascade operations. Empty list means no cascading. */
	private List<CascadeStrategy> cascade = new ArrayList<>();

	/**
	 * Database foreign key constraint name.
	 * Auto-derived as {@code "fk_" + ownerTable + "_" + column} when not set.
	 */
	private String foreignKey;

	/**
	 * The field on the related entity that maps back to this entity.
	 * Required for {@code ONE_TO_MANY} ({@code mappedBy} attribute).
	 */
	private String mappedBy;

	public RelationType getType() {
		return type;
	}

	public void setType(RelationType type) {
		this.type = type;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public FetchStrategy getFetch() {
		return fetch;
	}

	public void setFetch(FetchStrategy fetch) {
		this.fetch = fetch;
	}

	public List<CascadeStrategy> getCascade() {
		return cascade;
	}

	public void setCascade(List<CascadeStrategy> cascade) {
		this.cascade = cascade;
	}

	public String getForeignKey() {
		return foreignKey;
	}

	public void setForeignKey(String foreignKey) {
		this.foreignKey = foreignKey;
	}

	public String getMappedBy() {
		return mappedBy;
	}

	public void setMappedBy(String mappedBy) {
		this.mappedBy = mappedBy;
	}
}
