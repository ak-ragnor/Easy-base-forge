package com.easybase.forge.core.generator.controller;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.config.GeneratorConfig;
import com.easybase.forge.core.config.PaginationMode;
import com.easybase.forge.core.config.ResponseWrapperConfig;
import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.generator.GeneratorUtils;
import com.easybase.forge.core.generator.TypeNameResolver;
import com.easybase.forge.core.model.*;
import com.squareup.javapoet.*;

/**
 * Generates the abstract {@code {Resource}ControllerBase} class for a resource.
 *
 * <p>This class is always overwritten on regeneration. It contains one method
 * per endpoint, each delegating to the injected {@code {Resource}ApiDelegate}.
 */
public class BaseControllerGenerator {

	private static final ClassName GET_MAPPING = ClassName.get("org.springframework.web.bind.annotation", "GetMapping");
	private static final ClassName POST_MAPPING =
			ClassName.get("org.springframework.web.bind.annotation", "PostMapping");
	private static final ClassName PUT_MAPPING = ClassName.get("org.springframework.web.bind.annotation", "PutMapping");
	private static final ClassName PATCH_MAPPING =
			ClassName.get("org.springframework.web.bind.annotation", "PatchMapping");
	private static final ClassName DELETE_MAPPING =
			ClassName.get("org.springframework.web.bind.annotation", "DeleteMapping");
	private static final ClassName PATH_VARIABLE =
			ClassName.get("org.springframework.web.bind.annotation", "PathVariable");
	private static final ClassName REQUEST_PARAM =
			ClassName.get("org.springframework.web.bind.annotation", "RequestParam");
	private static final ClassName REQUEST_BODY =
			ClassName.get("org.springframework.web.bind.annotation", "RequestBody");
	private static final ClassName RESPONSE_STATUS =
			ClassName.get("org.springframework.web.bind.annotation", "ResponseStatus");
	private static final ClassName HTTP_STATUS = ClassName.get("org.springframework.http", "HttpStatus");
	private static final ClassName VALID = ClassName.get("jakarta.validation", "Valid");

	public GeneratedArtifact generate(ApiResource resource, GeneratorConfig config) {
		String basePkg =
				config.resolvePackage(config.getStructure().getController().getBasePkg(), resource.packageSuffix());
		String delegatePkg =
				config.resolvePackage(config.getStructure().getDelegate().getPkg(), resource.packageSuffix());
		String dtoPkg = config.resolvePackage(config.getStructure().getDto().getPkg(), resource.packageSuffix());

		TypeNameResolver typeResolver = new TypeNameResolver(dtoPkg);

		String delegateName = resource.name() + "ApiDelegate";
		String baseName = resource.name() + "ControllerBase";
		ClassName delegateType = ClassName.get(delegatePkg, delegateName);

		TypeSpec.Builder classBuilder = TypeSpec.classBuilder(baseName)
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addField(FieldSpec.builder(delegateType, "delegate", Modifier.PRIVATE, Modifier.FINAL)
						.build())
				.addMethod(MethodSpec.constructorBuilder()
						.addModifiers(Modifier.PROTECTED)
						.addParameter(delegateType, "delegate")
						.addStatement("this.delegate = delegate")
						.build())
				.addMethod(MethodSpec.methodBuilder("getDelegate")
						.addModifiers(Modifier.PROTECTED)
						.returns(delegateType)
						.addStatement("return delegate")
						.build());

		GeneratorUtils.addGeneratedJavadoc(classBuilder, config);

		for (ApiEndpoint endpoint : resource.endpoints()) {
			classBuilder.addMethod(buildEndpointMethod(endpoint, typeResolver, delegateType, config));
		}

		JavaFile javaFile = JavaFile.builder(basePkg, classBuilder.build())
				.skipJavaLangImports(true)
				.indent("    ")
				.build();

		Path outputPath = GeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), basePkg)
				.resolve(baseName + ".java");

		return new GeneratedArtifact(outputPath, ArtifactType.BASE_CONTROLLER, javaFile.toString());
	}

	private MethodSpec buildEndpointMethod(
			ApiEndpoint endpoint, TypeNameResolver typeResolver, ClassName delegateType, GeneratorConfig config) {

		PaginationMode paginationMode = config.getGenerate().getPagination();
		ResponseWrapperConfig wrapper = config.getGenerate().getResponseWrapper();

		TypeName returnType = typeResolver.resolveReturnType(
				endpoint, config.getGenerate().getResponseEntityWrapping(), wrapper, paginationMode);

		boolean applyPagination = endpoint.paginated() && paginationMode == PaginationMode.SPRING_DATA;

		MethodSpec.Builder mb = MethodSpec.methodBuilder(endpoint.operationId())
				.addModifiers(Modifier.PUBLIC)
				.returns(returnType)
				.addAnnotation(mappingAnnotation(endpoint.httpMethod(), endpoint.path()));

		if (wrapper != null && wrapper.isEnabled()) {
			int statusCode = endpoint.primaryResponse() != null
					? endpoint.primaryResponse().statusCode()
					: 200;

			if (statusCode == 201) {
				mb.addAnnotation(AnnotationSpec.builder(RESPONSE_STATUS)
						.addMember("value", "$T.CREATED", HTTP_STATUS)
						.build());
			} else if (statusCode == 204) {
				mb.addAnnotation(AnnotationSpec.builder(RESPONSE_STATUS)
						.addMember("value", "$T.NO_CONTENT", HTTP_STATUS)
						.build());
			}
		}

		List<String> delegateArgs = new ArrayList<>();

		for (ApiParameter param : endpoint.parameters()) {
			if (param.in() == ParameterLocation.PATH || param.in() == ParameterLocation.QUERY) {
				String javaName = GeneratorUtils.sanitizeName(param.name());
				ParameterSpec.Builder pb = ParameterSpec.builder(
						typeResolver.resolve(param.schema().javaType()), javaName);

				if (param.in() == ParameterLocation.PATH) {
					pb.addAnnotation(AnnotationSpec.builder(PATH_VARIABLE)
							.addMember("value", "$S", param.name())
							.build());
				} else {
					AnnotationSpec.Builder qa =
							AnnotationSpec.builder(REQUEST_PARAM).addMember("value", "$S", param.name());

					if (!param.required()) {
						qa.addMember("required", "false");
					}

					pb.addAnnotation(qa.build());
				}
				mb.addParameter(pb.build());
				delegateArgs.add(javaName);
			}
		}

		// Request body
		if (endpoint.requestBody() != null) {
			String bodyType = endpoint.requestBody().schema().javaType();
			String bodyName = GeneratorUtils.deriveBodyParamName(bodyType);

			ParameterSpec.Builder pb = ParameterSpec.builder(typeResolver.resolve(bodyType), bodyName)
					.addAnnotation(VALID)
					.addAnnotation(AnnotationSpec.builder(REQUEST_BODY)
							.addMember("required", "$L", endpoint.requestBody().required())
							.build());

			mb.addParameter(pb.build());
			delegateArgs.add(bodyName);
		}

		// Spring Data Pageable parameter (appended last)
		if (applyPagination) {
			mb.addParameter(TypeNameResolver.pageableType(), "pageable");
			delegateArgs.add("pageable");
		}

		String argList = String.join(", ", delegateArgs);
		String delegateCall = "delegate." + endpoint.operationId() + "(" + argList + ")";

		if (config.getGenerate().isAddGeneratedAnnotation()) {
			mb.addJavadoc("<pre>$L</pre>\n", buildCurlSnippet(endpoint));
		}

		boolean isVoid = returnType.equals(TypeName.VOID);

		if (isVoid) {
			mb.addStatement(delegateCall);
		} else {
			mb.addStatement("return $L", delegateCall);
		}

		return mb.build();
	}

	private static String buildCurlSnippet(ApiEndpoint endpoint) {
		StringBuilder sb = new StringBuilder("curl -X ")
				.append(endpoint.httpMethod().name())
				.append(" http://localhost:8080")
				.append(endpoint.path());

		if (endpoint.requestBody() != null) {
			sb.append(" \\\n  -H 'Content-Type: application/json' \\\n  -d '{}'");
		}

		return sb.toString();
	}

	private static AnnotationSpec mappingAnnotation(HttpMethod method, String path) {
		ClassName annotation =
				switch (method) {
					case GET -> GET_MAPPING;
					case POST -> POST_MAPPING;
					case PUT -> PUT_MAPPING;
					case PATCH -> PATCH_MAPPING;
					case DELETE -> DELETE_MAPPING;
					default -> GET_MAPPING;
				};
		return AnnotationSpec.builder(annotation).addMember("value", "$S", path).build();
	}
}
