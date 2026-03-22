package com.easybase.forge.core.writer;

import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.generator.controller.BaseControllerGenerator;
import com.easybase.forge.core.generator.controller.CustomControllerGenerator;
import com.easybase.forge.core.generator.delegate.DelegateGenerator;
import com.easybase.forge.core.generator.dto.DtoGenerator;
import com.easybase.forge.core.model.ApiResource;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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
    private final BaseControllerGenerator baseCtrlGen = new BaseControllerGenerator();
    private final CustomControllerGenerator customCtrlGen = new CustomControllerGenerator();

    public List<GenerationUnit> build(List<ApiResource> resources, GeneratorConfig config) {
        List<GenerationUnit> units = new ArrayList<>();

        for (ApiResource resource : resources) {
            // DTOs — always overwrite
            for (GeneratedArtifact dto : dtoGen.generate(resource, config)) {
                units.add(new GenerationUnit(dto, true));
            }

            // Delegate — always overwrite
            units.add(new GenerationUnit(delegateGen.generate(resource, config), true));

            // Base controller — always overwrite
            units.add(new GenerationUnit(baseCtrlGen.generate(resource, config), true));

            // Custom controller — create only (preserve if exists)
            GeneratedArtifact customCtrl = customCtrlGen.generate(resource, config);
            boolean customExists = Files.exists(customCtrl.outputPath());
            units.add(new GenerationUnit(customCtrl, !customExists));
        }

        return units;
    }
}
