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

public class HookGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		if (!config.getHook().isEnabled()) {
			return Collections.emptyList();
		}

		String pkg = ServiceGeneratorUtils.hookPackage(config);
		String hookName = ServiceGeneratorUtils.hookName(config);
		String hookBasePkg = ServiceGeneratorUtils.hookBasePackage(config);
		String hookBaseName = ServiceGeneratorUtils.hookBaseName(config);
		String entityName = ServiceGeneratorUtils.entityName(config);

		ClassName hookBaseType = ClassName.get(hookBasePkg, hookBaseName);

		TypeSpec.Builder builder = TypeSpec.classBuilder(hookName)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(hookBaseType)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("org.springframework.stereotype", "Component"))
						.build())
				.addJavadoc(
						"Developer-owned hook for {@code $L}.\n\n"
								+ "<p>Override methods from {@link $T} to add lifecycle behaviour.\n"
								+ "Example: populate {@code createdBy} from the security context in "
								+ "{@code beforeSave}.\n\n"
								+ "<p>This file is generated once and never overwritten.\n",
						entityName,
						hookBaseType);

		ServiceGeneratorUtils.applyAuthors(builder, config);
		TypeSpec hook = builder.build();

		JavaFile javaFile =
				JavaFile.builder(pkg, hook).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(hookName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_HOOK, javaFile.toString()));
	}
}
