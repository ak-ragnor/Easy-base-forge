package com.easybase.forge.core.service.config;

/**
 * Controls which CRUD operations are generated for this entity.
 *
 * <p>All operations default to {@code true}. Set any to {@code false} to omit the
 * corresponding methods from the generated service layer.
 *
 * <p>Example YAML:
 * <pre>
 * crud:
 *   create: true
 *   update: true
 *   delete: false
 *   get: true
 *   list: true
 * </pre>
 */
public class CrudOptions {

	private boolean create = true;
	private boolean update = true;
	private boolean delete = true;
	private boolean get = true;
	private boolean list = true;

	public boolean isCreate() {
		return create;
	}

	public void setCreate(boolean create) {
		this.create = create;
	}

	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public boolean isGet() {
		return get;
	}

	public void setGet(boolean get) {
		this.get = get;
	}

	public boolean isList() {
		return list;
	}

	public void setList(boolean list) {
		this.list = list;
	}
}
