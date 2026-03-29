package com.easybase.forge.core.model;

public enum ArtifactType {

	/** Generated abstract base controller — always overwritten. */
	BASE_CONTROLLER(true),

	/** User-owned controller extending the base — only created, never overwritten. */
	CUSTOM_CONTROLLER(false),

	/** Generated delegate interface — always overwritten. */
	DELEGATE(true),

	/** Generated DTO class — always overwritten. */
	DTO(true),

	/** Generated abstract stub base for delegate impl — always overwritten. */
	DELEGATE_IMPL_BASE(true),

	/** User-owned delegate implementation extending the base — only created, never overwritten. */
	DELEGATE_IMPL(false),

	// -------------------------------------------------------------------------
	// Service Builder artifact types
	// -------------------------------------------------------------------------

	/** Generated @MappedSuperclass with audit fields — always overwritten. */
	SERVICE_BASE_ENTITY(true),

	/** Generated immutable domain record — always overwritten. */
	SERVICE_MODEL(true),

	/** Generated repository interface extending BaseRepository — always overwritten. */
	SERVICE_REPOSITORY(true),

	/** Generated Spring Data JpaRepository interface — always overwritten. */
	SERVICE_JPA_REPOSITORY(true),

	/** Generated @Entity class — always overwritten. */
	SERVICE_ENTITY(true),

	/** Generated persistence adapter implementing the repository — always overwritten. */
	SERVICE_PERSISTENCE_ADAPTER(true),

	/** Generated base service interface — always overwritten. */
	SERVICE_BASE_SERVICE(true),

	/** Generated abstract base service implementation with hook wiring — always overwritten. */
	SERVICE_BASE_SERVICE_IMPL(true),

	/** Developer-owned service interface — only created, never overwritten. */
	SERVICE_USER_SERVICE(false),

	/** Developer-owned service implementation — only created, never overwritten. */
	SERVICE_USER_SERVICE_IMPL(false),

	/** Generated hook interface — always overwritten. */
	SERVICE_HOOK(true),

	/** Developer-owned hook implementation — only created, never overwritten. */
	SERVICE_HOOK_IMPL(false);

	private final boolean alwaysOverwrite;

	ArtifactType(boolean alwaysOverwrite) {
		this.alwaysOverwrite = alwaysOverwrite;
	}

	public boolean shouldAlwaysOverwrite() {
		return alwaysOverwrite;
	}
}
