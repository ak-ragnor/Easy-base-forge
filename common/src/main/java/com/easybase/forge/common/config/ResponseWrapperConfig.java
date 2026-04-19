package com.easybase.forge.common.config;

/** Configuration for a custom response wrapper class (e.g. {@code ApiResponse<T>}). */
public class ResponseWrapperConfig {

	private boolean enabled = false;
	private String singleClass;
	private String pagedClass;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getSingleClass() {
		return singleClass;
	}

	public void setSingleClass(String singleClass) {
		this.singleClass = singleClass;
	}

	public String getPagedClass() {
		return pagedClass;
	}

	public void setPagedClass(String pagedClass) {
		this.pagedClass = pagedClass;
	}
}
