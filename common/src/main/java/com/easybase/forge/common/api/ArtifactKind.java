package com.easybase.forge.common.api;

/**
 * Marker interface for enum types that classify generated artifacts.
 *
 * <p>Implementations declare whether an artifact is always regenerated or
 * only created on the first run (never overwritten).
 */
public interface ArtifactKind {

	/**
	 * Returns {@code true} if the artifact should be overwritten on every
	 * generation run, or {@code false} if it should only be created when it
	 * does not yet exist.
	 */
	boolean shouldAlwaysOverwrite();
}
