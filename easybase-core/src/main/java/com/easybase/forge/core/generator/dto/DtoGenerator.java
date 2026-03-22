package com.easybase.forge.core.generator.dto;

import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.generator.AnnotationBuilder;
import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.generator.GeneratorUtils;
import com.easybase.forge.core.generator.TypeNameResolver;
import com.easybase.forge.core.model.ArtifactType;
import com.easybase.forge.core.model.ApiResource;
import com.easybase.forge.core.model.DtoField;
import com.easybase.forge.core.model.DtoSchema;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates one {@code @Data} Lombok DTO class per {@link DtoSchema} in a resource.
 */
public class DtoGenerator {

    public List<GeneratedArtifact> generate(ApiResource resource, GeneratorConfig config) {
        String dtoPkg = config.resolvePackage(
                config.getStructure().getDto().getPkg(),
                resource.packageSuffix());
        TypeNameResolver typeResolver = new TypeNameResolver(dtoPkg);

        List<GeneratedArtifact> artifacts = new ArrayList<>();
        for (DtoSchema schema : resource.dtoSchemas()) {
            GeneratedArtifact artifact = generateDto(schema, dtoPkg, typeResolver, config);
            Path outputPath = GeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), dtoPkg)
                    .resolve(schema.className() + ".java");
            artifacts.add(new GeneratedArtifact(outputPath, ArtifactType.DTO, artifact.content()));
        }
        return artifacts;
    }

    private GeneratedArtifact generateDto(DtoSchema schema, String dtoPkg,
                                           TypeNameResolver typeResolver, GeneratorConfig config) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.className())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(ClassName.get("lombok", "Data"));

        for (DtoField field : schema.fields()) {
            FieldSpec.Builder fb = FieldSpec.builder(
                    typeResolver.resolve(field.javaType()),
                    field.name(),
                    Modifier.PRIVATE);

            // Validation annotations first (before @JsonProperty, consistent ordering)
            if (config.getGenerate().isBeanValidation()) {
                for (var constraint : field.validations()) {
                    fb.addAnnotation(AnnotationBuilder.build(constraint));
                }
            }

            // @JsonProperty — always added to preserve original spec names
            fb.addAnnotation(AnnotationSpec.builder(
                            ClassName.get("com.fasterxml.jackson.annotation", "JsonProperty"))
                    .addMember("value", "$S", field.jsonName())
                    .build());

            classBuilder.addField(fb.build());
        }

        JavaFile javaFile = JavaFile.builder(dtoPkg, classBuilder.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();

        // Return a placeholder artifact — caller sets the actual outputPath
        return new GeneratedArtifact(Path.of(""), ArtifactType.DTO, javaFile.toString());
    }

}
