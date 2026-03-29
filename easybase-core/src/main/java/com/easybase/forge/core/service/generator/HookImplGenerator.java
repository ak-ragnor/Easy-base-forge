package com.easybase.forge.core.service.generator;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.model.ArtifactType;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

/**
 * Generates the developer-owned default hook implementation for the configured entity.
 *
 * <p>The generated class is a Spring {@code @Component} that implements the hook interface.
 * Since all methods have default no-op implementations in {@link com.easybase.service.runtime.BaseHook},
 * the generated class body is empty — developers add only the overrides they need.
 *
 * <p>This file is generated once and <strong>never overwritten</strong> on subsequent runs,
 * preserving any developer customizations.
 *
 * <p>Returns an empty list when {@code hook.enabled = false}.
 *
 * <p>Example output for entity {@code User}:
 * <pre>
 * {@literal @}Component
 * public class UserHookImpl implements UserHook {
 *     // Override methods from UserHook to add lifecycle behaviour.
 *     // Example: set createdBy from the security context in beforeCreate.
 * }
 * </pre>
 */
public class HookImplGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		if (!config.getHook().isEnabled()) {
			return Collections.emptyList();
		}

		String pkg = ServiceGeneratorUtils.hookPackage(config);
		String hookImplName = ServiceGeneratorUtils.hookImplName(config);
		String hookName = ServiceGeneratorUtils.hookName(config);
		String entityName = ServiceGeneratorUtils.entityName(config);

		ClassName hookInterface = ClassName.get(pkg, hookName);
		AnnotationSpec componentAnnotation = AnnotationSpec.builder(
						ClassName.get("org.springframework.stereotype", "Component"))
				.build();

		TypeSpec hookImpl = TypeSpec.classBuilder(hookImplName)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(hookInterface)
				.addAnnotation(componentAnnotation)
				.addJavadoc(
						"Default no-op hook implementation for {@code $L}.\n\n"
								+ "<p>Override methods from {@link $T} to add lifecycle behaviour.\n\n"
								+ "<p>This file is generated once and never overwritten. "
								+ "Add your custom hook logic here.\n",
						entityName,
						hookInterface)
				.build();

		JavaFile javaFile =
				JavaFile.builder(pkg, hookImpl).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(hookImplName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_HOOK_IMPL, javaFile.toString()));
	}
}
