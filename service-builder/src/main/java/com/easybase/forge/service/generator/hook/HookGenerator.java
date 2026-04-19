package com.easybase.forge.service.generator.hook;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.common.util.PackageUtils;
import com.easybase.forge.service.config.ServiceConfig;
import com.easybase.forge.service.generator.ServiceArtifactGenerator;
import com.easybase.forge.service.generator.ServiceArtifactType;
import com.easybase.forge.service.generator.ServiceMetaUtils;
import com.easybase.forge.service.generator.ServiceNamingUtils;
import com.easybase.forge.service.generator.ServicePackageUtils;
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

		String pkg = ServicePackageUtils.hookPackage(config);
		String hookName = ServiceNamingUtils.hookName(config);
		String hookBasePkg = ServicePackageUtils.hookBasePackage(config);
		String hookBaseName = ServiceNamingUtils.hookBaseName(config);
		String entityName = ServiceNamingUtils.entityName(config);

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

		ServiceMetaUtils.applyAuthors(builder, config);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
		Path outputPath = PackageUtils.packageToPath(config.getResolvedOutputDirectory(), pkg)
				.resolve(hookName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ServiceArtifactType.SERVICE_HOOK, javaFile.toString()));
	}
}
