package com.easybase.forge.core.writer;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.model.ArtifactType;

import java.nio.file.Path;

/**
 * Pairs a {@link GeneratedArtifact} with the decision of whether it should overwrite
 * an existing file on disk.
 *
 * <p>The overwrite flag is decided in {@link GenerationPlan} before any I/O occurs,
 * centralising regeneration-safety logic in one place.
 */
public record GenerationUnit(
        GeneratedArtifact artifact,
        /** True = write unconditionally. False = skip if the target file already exists. */
        boolean overwrite
) {
    public Path outputPath() { return artifact.outputPath(); }
    public ArtifactType artifactType() { return artifact.artifactType(); }
    public String content() { return artifact.content(); }
}
