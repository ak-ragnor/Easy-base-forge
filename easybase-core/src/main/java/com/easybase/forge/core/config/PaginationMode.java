package com.easybase.forge.core.config;

public enum PaginationMode {
    /** No pagination support generated. Default. */
    NONE,
    /** Inject Spring Data Pageable parameter and return Page&lt;T&gt; for paginated operations. */
    SPRING_DATA
}
