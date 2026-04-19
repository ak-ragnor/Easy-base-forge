package com.easybase.forge.service.config;

/**
 * JPA fetch strategy for relationship fields.
 *
 * <p>The default for all generated relationships is {@link #LAZY} to prevent
 * accidental N+1 query issues.
 */
public enum FetchStrategy {

	/** Load the relationship only when accessed. Recommended default. */
	LAZY,

	/** Load the relationship eagerly with the owning entity. */
	EAGER
}
