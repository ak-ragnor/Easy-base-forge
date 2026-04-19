package com.easybase.forge.service.generator;

import com.easybase.forge.common.api.ArtifactKind;

public enum ServiceArtifactType implements ArtifactKind {
	SERVICE_MODEL_BASE(true),
	SERVICE_MODEL(false),
	SERVICE_ENTITY(true),
	SERVICE_REPOSITORY_BASE(true),
	SERVICE_REPOSITORY(false),
	SERVICE_JPA_REPOSITORY_BASE(true),
	SERVICE_JPA_REPOSITORY(false),
	SERVICE_PERSISTENCE_ADAPTER_BASE(true),
	SERVICE_PERSISTENCE_ADAPTER(false),
	SERVICE_HOOK_BASE(true),
	SERVICE_HOOK(false),
	SERVICE_SERVICE_BASE(true),
	SERVICE_SERVICE_BASE_IMPL(true),
	SERVICE_LOCAL_SERVICE_BASE(true),
	SERVICE_LOCAL_SERVICE_BASE_IMPL(true),
	SERVICE_LOCAL_SERVICE(false),
	SERVICE_SERVICE(false);

	private final boolean alwaysOverwrite;

	ServiceArtifactType(boolean alwaysOverwrite) {
		this.alwaysOverwrite = alwaysOverwrite;
	}

	@Override
	public boolean shouldAlwaysOverwrite() {
		return alwaysOverwrite;
	}
}
