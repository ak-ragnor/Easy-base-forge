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
	DELEGATE_IMPL(false);

	private final boolean alwaysOverwrite;

	ArtifactType(boolean alwaysOverwrite) {
		this.alwaysOverwrite = alwaysOverwrite;
	}

	public boolean shouldAlwaysOverwrite() {
		return alwaysOverwrite;
	}
}
