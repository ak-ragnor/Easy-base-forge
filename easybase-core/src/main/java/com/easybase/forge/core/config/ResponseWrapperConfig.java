package com.easybase.forge.core.config;

/**
 * Maps the {@code generate.responseWrapper} block in {@code easybase-config.yaml}.
 *
 * <p>When enabled, all generated methods return the configured wrapper types
 * (e.g., {@code ApiResponse<T>} / {@code ApiPageResponse<T>}) instead of
 * {@code ResponseEntity<T>}.  Set {@code generate.responseEntityWrapping: NEVER}
 * alongside this to avoid conflicts.
 */
public class ResponseWrapperConfig {

	private boolean enabled = false;

	/**
	 * Fully-qualified class name of the single-item wrapper, e.g.
	 * {@code "com.easybase.infrastructure.api.dto.response.ApiResponse"}.
	 */
	private String singleClass;

	/**
	 * Fully-qualified class name of the paginated wrapper, e.g.
	 * {@code "com.easybase.infrastructure.api.dto.response.ApiPageResponse"}.
	 */
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
