package com.easybase.forge.core.generator.dto;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.generator.AnnotationBuilder;
import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.generator.GeneratorUtils;
import com.easybase.forge.core.generator.TypeNameResolver;
import com.easybase.forge.core.model.*;
import com.squareup.javapoet.*;

/**
 * Generates one DTO class per {@link DtoSchema} in a resource.
 */
public class DtoGenerator {

	private static final ClassName JSON_TYPE_INFO = ClassName.get("com.fasterxml.jackson.annotation", "JsonTypeInfo");
	private static final ClassName JSON_SUB_TYPES = ClassName.get("com.fasterxml.jackson.annotation", "JsonSubTypes");
	private static final ClassName JSON_SUB_TYPE =
			ClassName.get("com.fasterxml.jackson.annotation", "JsonSubTypes", "Type");
	private static final ClassName JSON_PROPERTY = ClassName.get("com.fasterxml.jackson.annotation", "JsonProperty");
	private static final ClassName JSON_PROPERTY_ACCESS =
			ClassName.get("com.fasterxml.jackson.annotation", "JsonProperty", "Access");
	private static final ClassName NULLABLE = ClassName.get("org.springframework.lang", "Nullable");

	public List<GeneratedArtifact> generate(ApiResource resource, GeneratorConfig config) {
		String dtoPkg = config.resolvePackage(config.getStructure().getDto().getPkg(), resource.packageSuffix());
		TypeNameResolver typeResolver = new TypeNameResolver(dtoPkg);
		List<GeneratedArtifact> artifacts = new ArrayList<>();

		for (DtoSchema schema : resource.dtoSchemas()) {
			String content = schema.union() != null
					? generateUnionBase(schema, dtoPkg, config)
					: generateDto(schema, dtoPkg, typeResolver, config);

			Path outputPath = GeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), dtoPkg)
					.resolve(schema.className() + ".java");
			artifacts.add(new GeneratedArtifact(outputPath, ArtifactType.DTO, content));
		}

		return artifacts;
	}

	private String generateDto(DtoSchema schema, String dtoPkg, TypeNameResolver typeResolver, GeneratorConfig config) {
		TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.className())
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(ClassName.get("lombok", "Data"));

		addGeneratedJavadoc(classBuilder, config);

		if (schema.parentClass() != null) {
			classBuilder.superclass(ClassName.get(dtoPkg, schema.parentClass()));
		}

		for (DtoField field : schema.fields()) {
			FieldSpec.Builder fb =
					FieldSpec.builder(typeResolver.resolve(field.javaType()), field.name(), Modifier.PRIVATE);

			if (field.nullable()) {
				fb.addAnnotation(NULLABLE);
			}

			// Validation annotations (before @JsonProperty for consistent ordering)
			if (config.getGenerate().isBeanValidation()) {
				for (var constraint : field.validations()) {
					fb.addAnnotation(AnnotationBuilder.build(constraint));
				}
			}

			AnnotationSpec.Builder jpBuilder =
					AnnotationSpec.builder(JSON_PROPERTY).addMember("value", "$S", field.jsonName());

			if (field.readOnly()) {
				jpBuilder.addMember("access", "$T.READ_ONLY", JSON_PROPERTY_ACCESS);
			}

			fb.addAnnotation(jpBuilder.build());

			classBuilder.addField(fb.build());
		}

		JavaFile javaFile = JavaFile.builder(dtoPkg, classBuilder.build())
				.skipJavaLangImports(true)
				.indent("    ")
				.build();

		return javaFile.toString();
	}

	private static void addGeneratedJavadoc(TypeSpec.Builder classBuilder, GeneratorConfig config) {
		if (!config.getGenerate().isAddGeneratedAnnotation()) {
			return;
		}

		StringBuilder doc = new StringBuilder();

		for (String author : config.getGenerate().getAllAuthors()) {
			doc.append("@author ").append(author).append("\n");
		}

		doc.append("@generated\n");

		classBuilder.addJavadoc(doc.toString());
	}

	private String generateUnionBase(DtoSchema schema, String dtoPkg, GeneratorConfig config) {
		UnionDiscriminator union = schema.union();

		AnnotationSpec jsonTypeInfo = AnnotationSpec.builder(JSON_TYPE_INFO)
				.addMember("use", "$T.Id.NAME", JSON_TYPE_INFO)
				.addMember("include", "$T.As.PROPERTY", JSON_TYPE_INFO)
				.addMember("property", "$S", union.propertyName())
				.build();

		AnnotationSpec.Builder jsonSubTypes = AnnotationSpec.builder(JSON_SUB_TYPES);
		for (UnionDiscriminator.SubtypeMapping m : union.subtypes()) {
			AnnotationSpec typeAnnotation = AnnotationSpec.builder(JSON_SUB_TYPE)
					.addMember("value", "$T.class", ClassName.get(dtoPkg, m.className()))
					.addMember("name", "$S", m.discriminatorValue())
					.build();
			jsonSubTypes.addMember("value", "$L", typeAnnotation);
		}

		TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(schema.className())
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addAnnotation(jsonTypeInfo)
				.addAnnotation(jsonSubTypes.build());

		addGeneratedJavadoc(typeBuilder, config);

		return JavaFile.builder(dtoPkg, typeBuilder.build())
				.skipJavaLangImports(true)
				.indent("    ")
				.build()
				.toString();
	}
}
