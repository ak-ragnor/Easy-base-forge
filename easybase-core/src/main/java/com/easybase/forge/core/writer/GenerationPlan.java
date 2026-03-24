package com.easybase.forge.core.writer;

import com.easybase.forge.core.config.ConfigException;
import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.config.LayoutMode;
import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.generator.controller.BaseControllerGenerator;
import com.easybase.forge.core.model.ArtifactType;
import com.easybase.forge.core.generator.controller.CustomControllerGenerator;
import com.easybase.forge.core.generator.delegate.DelegateGenerator;
import com.easybase.forge.core.generator.delegate.DelegateImplGenerator;
import com.easybase.forge.core.generator.dto.DtoGenerator;
import com.easybase.forge.core.model.ApiResource;
import com.easybase.forge.core.model.DtoSchema;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the list of {@link GenerationUnit}s for a full generation run.
 *
 * <p>This is where regeneration-safety is decided: the overwrite flag on each unit
 * is set based on the artifact type and whether the output file already exists.
 *
 * <table>
 *   <tr><th>Artifact type</th><th>Overwrite behaviour</th></tr>
 *   <tr><td>BASE_CONTROLLER</td><td>Always overwrite</td></tr>
 *   <tr><td>DELEGATE</td><td>Always overwrite</td></tr>
 *   <tr><td>DTO</td><td>Always overwrite</td></tr>
 *   <tr><td>CUSTOM_CONTROLLER</td><td>Only create; never overwrite existing</td></tr>
 * </table>
 */
public class GenerationPlan {

    private final DtoGenerator dtoGen = new DtoGenerator();
    private final DelegateGenerator delegateGen = new DelegateGenerator();
    private final DelegateImplGenerator delegateImplGen = new DelegateImplGenerator();
    private final BaseControllerGenerator baseCtrlGen = new BaseControllerGenerator();
    private final CustomControllerGenerator customCtrlGen = new CustomControllerGenerator();

    public List<GenerationUnit> build(List<ApiResource> resources, GeneratorConfig config) {
        List<GenerationUnit> units = new ArrayList<>();

        if (config.getLayoutStrategy().mode() == LayoutMode.MULTI_MODULE) {
            // Multi-module layout: detect schemas shared across resources (would produce duplicate classes)
            checkMultiModuleConflicts(resources);
        }

        // In FLAT layout, multiple resources may reference the same shared schema — deduplicate by output path
        Set<String> usedDtoPaths = new HashSet<>();

        for (ApiResource resource : resources) {
            // DTOs — always overwrite (skip if already queued from a previous resource in FLAT layout)
            for (GeneratedArtifact dto : dtoGen.generate(resource, config)) {
                if (usedDtoPaths.add(dto.outputPath().toString())) {
                    units.add(new GenerationUnit(dto, true));
                }
            }

            // Delegate — always overwrite
            units.add(new GenerationUnit(delegateGen.generate(resource, config), true));

            // Delegate impl — base always overwritten; user impl create-once
            if (config.getGenerate().isDelegateImpl()) {
                for (GeneratedArtifact artifact : delegateImplGen.generate(resource, config)) {
                    boolean isBase = artifact.artifactType() == ArtifactType.DELEGATE_IMPL_BASE;
                    boolean overwrite = isBase || !Files.exists(artifact.outputPath());
                    units.add(new GenerationUnit(artifact, overwrite));
                }
            }

            // Base controller — always overwrite
            units.add(new GenerationUnit(baseCtrlGen.generate(resource, config), true));

            // Custom controller — create only (preserve if exists)
            GeneratedArtifact customCtrl = customCtrlGen.generate(resource, config);
            boolean customExists = Files.exists(customCtrl.outputPath());
            units.add(new GenerationUnit(customCtrl, !customExists));
        }

        return units;
    }

    /**
     * Verifies that no two resources share the same DTO schema name in MULTI_MODULE layout.
     *
     * <p>MULTI_MODULE generates a separate DTO package per resource, so a schema referenced
     * by two tags would produce two incompatible classes in different packages.
     * Users should switch to FLAT layout to share schemas across tags.
     *
     * @throws ConfigException if a shared schema is detected
     */
    private void checkMultiModuleConflicts(List<ApiResource> resources) {
        Map<String, String> seen = new HashMap<>();
        for (ApiResource resource : resources) {
            for (DtoSchema schema : resource.dtoSchemas()) {
                String existing = seen.put(schema.className(), resource.name());
                if (existing != null) {
                    throw new ConfigException(
                            "MULTI_MODULE layout: schema '" + schema.className() + "' is shared by "
                            + "resources '" + existing + "' and '" + resource.name() + "'. "
                            + "MULTI_MODULE generates a separate DTO package per resource, so "
                            + "schemas cannot be shared across tags. "
                            + "Use 'output.layout: FLAT' instead.");
                }
            }
        }
    }

}
