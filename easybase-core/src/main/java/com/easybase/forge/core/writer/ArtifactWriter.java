package com.easybase.forge.core.writer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Writes {@link GenerationUnit}s to disk with regeneration-safe semantics.
 *
 * <p>Units with {@code overwrite=false} are skipped if the target file already exists.
 */
public class ArtifactWriter {

    public GenerationReport write(List<GenerationUnit> units) {
        GenerationReport.Builder report = GenerationReport.builder();

        for (GenerationUnit unit : units) {
            boolean exists = Files.exists(unit.outputPath());

            if (!unit.overwrite() && exists) {
                report.skipped(unit.outputPath().toString());
                continue;
            }

            try {
                Files.createDirectories(unit.outputPath().getParent());
                Files.writeString(unit.outputPath(), unit.content(), StandardCharsets.UTF_8);

                if (exists) {
                    report.updated(unit.outputPath().toString());
                } else {
                    report.created(unit.outputPath().toString());
                }
            } catch (IOException e) {
                report.error("Failed to write " + unit.outputPath() + ": " + e.getMessage());
            }
        }

        return report.build();
    }
}
