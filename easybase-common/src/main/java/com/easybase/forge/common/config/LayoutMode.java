package com.easybase.forge.common.config;

/**
 * Controls how generated packages are structured across resources.
 *
 * <ul>
 *   <li>{@link #FLAT} — all resources share top-level packages</li>
 *   <li>{@link #MULTI_MODULE} — each resource gets its own sub-package tree</li>
 * </ul>
 */
public enum LayoutMode {
	FLAT,
	MULTI_MODULE
}
