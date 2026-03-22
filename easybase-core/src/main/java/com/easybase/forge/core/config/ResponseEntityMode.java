package com.easybase.forge.core.config;

public enum ResponseEntityMode {
    /** All methods return ResponseEntity&lt;T&gt;. Default. */
    ALWAYS,
    /** All methods return the raw type T. */
    NEVER,
    /** Only void-returning operations use ResponseEntity&lt;Void&gt;; others return T. */
    VOID_ONLY
}
