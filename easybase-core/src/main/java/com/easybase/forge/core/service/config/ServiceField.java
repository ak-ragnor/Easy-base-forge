package com.easybase.forge.core.service.config;

/**
 * Describes one field on a generated entity.
 *
 * <p>Example YAML:
 * <pre>
 * fields:
 *   - name: email
 *     type: String
 *     required: true
 *     maxLength: 255
 *     unique: true
 * </pre>
 */
public class ServiceField {

	/** Java field name (camelCase). */
	private String name;

	/**
	 * Java type name.
	 * Supported values: {@code String}, {@code Integer}, {@code Long}, {@code Boolean},
	 * {@code BigDecimal}, {@code LocalDate}, {@code OffsetDateTime}, {@code UUID}.
	 */
	private String type;

	/** Whether the field is non-null. Maps to {@code @Column(nullable = false)} and {@code @NotNull}. */
	private boolean required = false;

	/** Maximum string length. Maps to {@code @Column(length = maxLength)} and {@code @Size(max = maxLength)}. */
	private Integer maxLength;

	/** Whether a unique constraint is applied to this column. */
	private boolean unique = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}
}
