package com.easybase.forge.common.io;

import java.nio.file.Path;

import com.easybase.forge.common.api.ArtifactKind;

/** The output of one generator invocation — a file path and its Java source content. */
public record GeneratedArtifact(Path outputPath, ArtifactKind artifactType, String content) {}
