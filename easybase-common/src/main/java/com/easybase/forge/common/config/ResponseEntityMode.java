package com.easybase.forge.common.config;

/** Controls whether generated methods wrap their return type in {@code ResponseEntity<T>}. */
public enum ResponseEntityMode {
	/** Every method returns {@code ResponseEntity<T>}. */
	ALWAYS,
	/** Methods return the raw type {@code T} (or {@code void}). */
	NEVER,
	/** Void methods return {@code ResponseEntity<Void>}; non-void return raw {@code T}. */
	VOID_ONLY
}
