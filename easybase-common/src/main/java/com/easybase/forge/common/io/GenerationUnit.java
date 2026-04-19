package com.easybase.forge.common.io;

import java.nio.file.Path;

import com.easybase.forge.common.api.ArtifactKind;

/**
 * Pairs a {@link GeneratedArtifact} with the decision of whether it should overwrite
 * an existing file on disk.
 *
 * <p>The overwrite flag is decided in the generation plan before any I/O occurs,
 * centralising regeneration-safety logic in one place.
 */
public record GenerationUnit(GeneratedArtifact artifact, boolean overwrite) {

	public Path outputPath() {
		return artifact.outputPath();
	}

	public ArtifactKind artifactType() {
		return artifact.artifactType();
	}

	public String content() {
		return artifact.content();
	}
}
