package com.easybase.forge.core.config;

/**
 * Maps the {@code output} block in {@code easybase-config.yaml}.
 */
public class OutputConfig {

    private String directory;
    private LayoutMode layout = LayoutMode.FLAT;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public LayoutMode getLayout() {
        return layout;
    }

    public void setLayout(LayoutMode layout) {
        this.layout = layout;
    }
}
