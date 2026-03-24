package com.easybase.forge.core.model;

public enum ArtifactType {
    /** Generated abstract base controller — always overwritten. */
    BASE_CONTROLLER,
    /** User-owned controller extending the base — only created, never overwritten. */
    CUSTOM_CONTROLLER,
    /** Generated delegate interface — always overwritten. */
    DELEGATE,
    /** Generated DTO class — always overwritten. */
    DTO,
    /** Generated abstract stub base for delegate impl — always overwritten. */
    DELEGATE_IMPL_BASE,
    /** User-owned delegate implementation extending the base — only created, never overwritten. */
    DELEGATE_IMPL
}
