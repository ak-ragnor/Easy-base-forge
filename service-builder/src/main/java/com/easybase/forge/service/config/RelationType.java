package com.easybase.forge.service.config;

/**
 * JPA relationship types supported by the Service Builder.
 *
 * <p>{@link #MANY_TO_MANY} is reserved for a future phase.
 */
public enum RelationType {

	/** Many-to-one: this entity holds the foreign key column. */
	MANY_TO_ONE,

	/** One-to-many: the related entity holds the foreign key (mappedBy side). */
	ONE_TO_MANY,

	/** One-to-one: this entity holds the foreign key column. */
	ONE_TO_ONE
}
