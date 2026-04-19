package com.easybase.forge.rest.generator;

import com.easybase.forge.common.api.ArtifactKind;

public enum RestArtifactType implements ArtifactKind {
	BASE_CONTROLLER(true),

	CUSTOM_CONTROLLER(false),

	DELEGATE(true),

	DTO(true),

	DELEGATE_IMPL_BASE(true),

	DELEGATE_IMPL(false);

	private final boolean alwaysOverwrite;

	RestArtifactType(boolean alwaysOverwrite) {
		this.alwaysOverwrite = alwaysOverwrite;
	}

	@Override
	public boolean shouldAlwaysOverwrite() {
		return alwaysOverwrite;
	}
}
