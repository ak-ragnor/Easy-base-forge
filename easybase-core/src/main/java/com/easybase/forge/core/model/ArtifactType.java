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

	/** Generated domain model base POJO (all generated fields) — always overwritten. */
	SERVICE_MODEL_BASE(true),

	/** Developer-owned domain model shell extending the base — only created, never overwritten. */
	SERVICE_MODEL(false),

	/** Generated @Entity JPA class — always overwritten. */
	SERVICE_ENTITY(true),

	/** Generated base repository interface extending BaseRepository — always overwritten. */
	SERVICE_REPOSITORY_BASE(true),

	/** Developer-owned repository extending the base — only created, never overwritten. */
	SERVICE_REPOSITORY(false),

	/** Generated @NoRepositoryBean Spring Data JPA base interface — always overwritten. */
	SERVICE_JPA_REPOSITORY_BASE(true),

	/** Developer-owned Spring Data JPA repository — only created, never overwritten. */
	SERVICE_JPA_REPOSITORY(false),

	/** Generated abstract persistence adapter base with hook wiring — always overwritten. */
	SERVICE_PERSISTENCE_ADAPTER_BASE(true),

	/** Developer-owned persistence adapter extending the base — only created, never overwritten. */
	SERVICE_PERSISTENCE_ADAPTER(false),

	/** Generated lifecycle hook base interface — always overwritten. */
	SERVICE_HOOK_BASE(true),

	/** Developer-owned hook implementation — only created, never overwritten. */
	SERVICE_HOOK(false),

	/** Generated base service interface declaring enabled CRUD operations — always overwritten. */
	SERVICE_SERVICE_BASE(true),

	/** Generated abstract base service implementation — always overwritten. */
	SERVICE_SERVICE_BASE_IMPL(true),

	/** Generated local service base interface extending the service base — always overwritten. */
	SERVICE_LOCAL_SERVICE_BASE(true),

	/** Generated abstract local service base implementation — always overwritten. */
	SERVICE_LOCAL_SERVICE_BASE_IMPL(true),

	/** Developer-owned local service (@Service) — only created, never overwritten. */
	SERVICE_LOCAL_SERVICE(false),

	/** Developer-owned service façade (@Component) — only created, never overwritten. */
	SERVICE_SERVICE(false);

	private final boolean alwaysOverwrite;

	ArtifactType(boolean alwaysOverwrite) {
		this.alwaysOverwrite = alwaysOverwrite;
	}

	public boolean shouldAlwaysOverwrite() {
		return alwaysOverwrite;
	}
}
