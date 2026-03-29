package com.easybase.forge.core.service.generator;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.model.ArtifactType;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Generates the lifecycle hook interface for the configured entity.
 *
 * <p>The generated interface extends {@link com.easybase.service.runtime.BaseHook} and
 * inherits all default no-op method implementations. Developers can create one or more
 * Spring beans implementing this interface to hook into CRUD operations.
 *
 * <p>Returns an empty list when {@code hook.enabled = false}.
 *
 * <p>Example output for entity {@code User}:
 * <pre>
 * public interface UserHook extends BaseHook&lt;User, UUID&gt; {}
 * </pre>
 */
public class HookGenerator implements ServiceArtifactGenerator {

	private static final String BASE_HOOK_PACKAGE = "com.easybase.service.runtime";
	private static final String BASE_HOOK_CLASS = "BaseHook";

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		if (!config.getHook().isEnabled()) {
			return Collections.emptyList();
		}

		String pkg = ServiceGeneratorUtils.hookPackage(config);
		String hookName = ServiceGeneratorUtils.hookName(config);
		String modelPkg = ServiceGeneratorUtils.modelPackage(config);
		String entityName = ServiceGeneratorUtils.entityName(config);

		ClassName entityType = ClassName.get(modelPkg, entityName);
		ClassName baseHook = ClassName.get(BASE_HOOK_PACKAGE, BASE_HOOK_CLASS);
		ParameterizedTypeName superInterface =
				ParameterizedTypeName.get(baseHook, entityType, ServiceGeneratorUtils.resolveIdType(config));

		TypeSpec hookInterface = TypeSpec.interfaceBuilder(hookName)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(superInterface)
				.addJavadoc(
						"Lifecycle hook interface for {@link $T}.\n\n"
								+ "<p>Implement this interface as a Spring {@code @Component} to receive\n"
								+ "callbacks before and after each CRUD operation. All methods have\n"
								+ "default no-op implementations — only override the events you need.\n\n"
								+ "<p>Use {@code beforeCreate} to populate audit fields such as\n"
								+ "{@code createdBy} from the current security context.\n",
						entityType)
				.build();

		JavaFile javaFile =
				JavaFile.builder(pkg, hookInterface).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(hookName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_HOOK, javaFile.toString()));
	}
}
