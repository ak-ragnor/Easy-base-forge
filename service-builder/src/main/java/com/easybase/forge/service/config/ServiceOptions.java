package com.easybase.forge.service.config;

/**
 * Top-level service generation options in {@code easybase.yml}.
 *
 * <p>Example YAML:
 * <pre>
 * service:
 *   type: local
 *   generateCrud: true
 * </pre>
 */
public class ServiceOptions {

	/**
	 * Service deployment type.
	 * Currently only {@code local} is supported. {@code remote} is reserved for future use.
	 */
	private String type = "local";

	/** Whether standard CRUD operations are generated. Default: {@code true}. */
	private boolean generateCrud = true;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isGenerateCrud() {
		return generateCrud;
	}

	public void setGenerateCrud(boolean generateCrud) {
		this.generateCrud = generateCrud;
	}
}
