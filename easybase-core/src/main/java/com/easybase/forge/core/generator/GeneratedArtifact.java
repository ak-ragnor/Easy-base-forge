package com.easybase.forge.core.generator;

import com.easybase.forge.core.model.ArtifactType;

import java.nio.file.Path;

/** The output of one generator invocation — a path and the Java source content. */
public record GeneratedArtifact(
        Path outputPath,
        ArtifactType artifactType,
        String content
) {}
