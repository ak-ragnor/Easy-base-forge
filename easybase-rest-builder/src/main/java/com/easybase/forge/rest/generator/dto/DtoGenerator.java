package com.easybase.forge.rest.generator.dto;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.common.config.GeneratorConfig;
import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.util.PackageUtils;
import com.easybase.forge.rest.generator.AnnotationBuilder;
import com.easybase.forge.rest.generator.RestArtifactGenerator;
import com.easybase.forge.rest.generator.RestArtifactType;
import com.easybase.forge.rest.generator.RestGeneratorUtils;
import com.easybase.forge.rest.generator.TypeNameResolver;
import com.easybase.forge.rest.model.ApiResource;
import com.easybase.forge.rest.model.DtoField;
import com.easybase.forge.rest.model.DtoSchema;
import com.easybase.forge.rest.model.UnionDiscriminator;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

public class DtoGenerator implements RestArtifactGenerator {

	private static final ClassName JSON_TYPE_INFO = ClassName.get("com.fasterxml.jackson.annotation", "JsonTypeInfo");
	private static final ClassName JSON_SUB_TYPES = ClassName.get("com.fasterxml.jackson.annotation", "JsonSubTypes");
	private static final ClassName JSON_SUB_TYPE =
			ClassName.get("com.fasterxml.jackson.annotation", "JsonSubTypes", "Type");
	private static final ClassName JSON_PROPERTY = ClassName.get("com.fasterxml.jackson.annotation", "JsonProperty");
	private static final ClassName JSON_PROPERTY_ACCESS =
			ClassName.get("com.fasterxml.jackson.annotation", "JsonProperty", "Access");
	private static final ClassName NULLABLE = ClassName.get("org.springframework.lang", "Nullable");

	@Override
	public List<GeneratedArtifact> generate(ApiResource resource, GeneratorConfig config) {
		String dtoPkg = config.resolvePackage(config.getStructure().getDto().getPkg(), resource.packageSuffix());
		TypeNameResolver typeResolver = new TypeNameResolver(dtoPkg);
		List<GeneratedArtifact> artifacts = new ArrayList<>();

		for (DtoSchema schema : resource.dtoSchemas()) {
			String content;

			if (schema.union() != null) {
				content = generateUnionBase(schema, dtoPkg, config);
			} else {
				content = generateDto(schema, dtoPkg, typeResolver, config);
			}

			Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), dtoPkg)
					.resolve(schema.className() + ".java");
			artifacts.add(new GeneratedArtifact(outputPath, RestArtifactType.DTO, content));
		}

		return artifacts;
	}

	private String generateDto(DtoSchema schema, String dtoPkg, TypeNameResolver typeResolver, GeneratorConfig config) {
		TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.className())
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(ClassName.get("lombok", "Data"));

		RestGeneratorUtils.addGeneratedJavadoc(classBuilder, config);

		if (schema.parentClass() != null) {
			classBuilder.superclass(ClassName.get(dtoPkg, schema.parentClass()));
		}

		for (DtoField field : schema.fields()) {
			FieldSpec.Builder fb =
					FieldSpec.builder(typeResolver.resolve(field.javaType()), field.name(), Modifier.PRIVATE);

			if (field.nullable()) {
				fb.addAnnotation(NULLABLE);
			}

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

		RestGeneratorUtils.addGeneratedJavadoc(typeBuilder, config);

		return JavaFile.builder(dtoPkg, typeBuilder.build())
				.skipJavaLangImports(true)
				.indent("    ")
				.build()
				.toString();
	}
}
