package com.easybase.forge.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps the {@code structure} block in {@code easybase-config.yaml}.
 */
public class StructureConfig {

    private ControllerStructureConfig controller = new ControllerStructureConfig();

    @JsonProperty("delegate")
    private PackageConfig delegate = new PackageConfig("{basePackage}.{resource}.delegate");

    @JsonProperty("dto")
    private PackageConfig dto = new PackageConfig("{basePackage}.{resource}.dto");

    public ControllerStructureConfig getController() {
        return controller;
    }

    public void setController(ControllerStructureConfig controller) {
        this.controller = controller;
    }

    public PackageConfig getDelegate() {
        return delegate;
    }

    public void setDelegate(PackageConfig delegate) {
        this.delegate = delegate;
    }

    public PackageConfig getDto() {
        return dto;
    }

    public void setDto(PackageConfig dto) {
        this.dto = dto;
    }

    /** Simple wrapper for a single {@code package} string entry. */
    public static class PackageConfig {

        @JsonProperty("package")
        private String pkg;

        public PackageConfig() {
        }

        public PackageConfig(String pkg) {
            this.pkg = pkg;
        }

        public String getPkg() {
            return pkg;
        }

        public void setPkg(String pkg) {
            this.pkg = pkg;
        }
    }
}
