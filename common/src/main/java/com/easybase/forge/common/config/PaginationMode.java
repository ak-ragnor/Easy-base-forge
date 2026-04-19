package com.easybase.forge.common.config;

/** Controls pagination support in generated controllers and delegates. */
public enum PaginationMode {
	/** No pagination support. */
	NONE,
	/** Inject Spring Data {@code Pageable} and return {@code Page<T>}. */
	SPRING_DATA
}
