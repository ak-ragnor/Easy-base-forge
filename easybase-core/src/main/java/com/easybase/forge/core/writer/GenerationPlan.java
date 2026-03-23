package com.easybase.forge.core.writer;

import com.easybase.forge.core.config.ConfigException;
import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.config.LayoutMode;
import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.generator.controller.BaseControllerGenerator;
import com.easybase.forge.core.generator.controller.CustomControllerGenerator;
import com.easybase.forge.core.generator.delegate.DelegateGenerator;
import com.easybase.forge.core.generator.delegate.DelegateImplGenerator;
import com.easybase.forge.core.generator.dto.DtoGenerator;
import com.easybase.forge.core.model.ApiResource;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // Flat layout: detect DTO class name collisions across resources before writing anything
        if (config.getLayoutStrategy().mode() == LayoutMode.FLAT) {
            checkFlatLayoutConflicts(resources, config);
        }

        for (ApiResource resource : resources) {
            // DTOs — always overwrite
            for (GeneratedArtifact dto : dtoGen.generate(resource, config)) {
                units.add(new GenerationUnit(dto, true));
            }

            // Delegate — always overwrite
            units.add(new GenerationUnit(delegateGen.generate(resource, config), true));

            // Delegate impl — create only (preserve if exists)
            if (config.getGenerate().isDelegateImpl()) {
                GeneratedArtifact delegateImpl = delegateImplGen.generate(resource, config);
                boolean implExists = Files.exists(delegateImpl.outputPath());
                units.add(new GenerationUnit(delegateImpl, !implExists));
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
     * Verifies that no two resources would produce a DTO with the same output path.
     *
     * <p>With flat layout all resources share the same packages, so a {@code PetDTO} from
     * resource {@code pets} and a {@code PetDTO} from resource {@code archive} would collide.
     *
     * @throws ConfigException if a collision is detected
     */
    private void checkFlatLayoutConflicts(List<ApiResource> resources, GeneratorConfig config) {
        // Map from output-path → first resource that claimed it
        Map<String, String> seen = new HashMap<>();

        for (ApiResource resource : resources) {
            for (GeneratedArtifact dto : dtoGen.generate(resource, config)) {
                String pathKey = dto.outputPath().toString();
                String existing = seen.put(pathKey, resource.name());
                if (existing != null) {
                    throw new ConfigException(
                            "Flat layout conflict: resources '" + existing + "' and '"
                            + resource.name() + "' both generate a DTO at '"
                            + dto.outputPath().getFileName()
                            + "'. Use MULTI_MODULE layout or rename one of the schemas.");
                }
            }
        }
    }
}
