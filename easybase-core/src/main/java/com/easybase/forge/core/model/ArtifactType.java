package com.easybase.forge.core.model;

public enum ArtifactType {
    /** Generated abstract base controller — always overwritten. */
    BASE_CONTROLLER,
    /** User-owned controller extending the base — only created, never overwritten. */
    CUSTOM_CONTROLLER,
    /** Generated delegate interface — always overwritten. */
    DELEGATE,
    /** Generated DTO class — always overwritten. */
    DTO
}
