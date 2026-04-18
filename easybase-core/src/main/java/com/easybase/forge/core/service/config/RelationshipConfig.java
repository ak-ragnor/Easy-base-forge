package com.easybase.forge.core.service.config;

import java.util.ArrayList;
import java.util.List;

public class RelationshipConfig {

	private RelationType type;

	private String entity;

	private String field;

	private String column;

	private boolean nullable = true;

	private String idType;

	private List<CascadeStrategy> cascade = new ArrayList<>();

	private String foreignKey;

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

	public String getIdType() {
		return idType;
	}

	public void setIdType(String idType) {
		this.idType = idType;
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
