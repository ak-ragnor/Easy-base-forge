package com.easybase.forge.core.service.config;

/**
 * JPA cascade types for relationship fields.
 *
 * <p>Maps directly to {@code jakarta.persistence.CascadeType}.
 */
public enum CascadeStrategy {

	/** Propagate all operations (persist, merge, remove, refresh, detach). */
	ALL,

	/** Propagate persist operations to related entities. */
	PERSIST,

	/** Propagate merge operations to related entities. */
	MERGE,

	/** Propagate remove (delete) operations to related entities. */
	REMOVE
}
