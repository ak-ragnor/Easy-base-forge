package com.easybase.forge.core.service.generator;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.model.ArtifactType;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

/**
 * Generates the developer-owned service interface for the configured entity.
 *
 * <p>This interface extends the generated {@code UserBaseService} and is the correct
 * place to declare business-level methods such as:
 * <pre>
 * User createUser(String email, String firstName, String lastName, UUID tenantId);
 * </pre>
 *
 * <p>This file is generated once and <strong>never overwritten</strong> on subsequent runs,
 * preserving any developer additions.
 *
 * <p>Example output for entity {@code User}:
 * <pre>
 * public interface UserService extends UserBaseService {
 *     // Add business methods here.
 * }
 * </pre>
 */
public class UserServiceGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.servicePackage(config);
		String serviceName = ServiceGeneratorUtils.serviceName(config);
		String baseServicePkg = ServiceGeneratorUtils.baseServicePackage(config);
		String baseServiceName = ServiceGeneratorUtils.baseServiceName(config);
		String entityName = ServiceGeneratorUtils.entityName(config);

		ClassName baseServiceType = ClassName.get(baseServicePkg, baseServiceName);

		TypeSpec serviceInterface = TypeSpec.interfaceBuilder(serviceName)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(baseServiceType)
				.addJavadoc(
						"Developer-owned service interface for {@code $L}.\n\n"
								+ "<p>Add business-level methods here. Example:\n"
								+ "<pre>\n"
								+ "$L create$L(String email, ...);\n"
								+ "</pre>\n\n"
								+ "<p>This file is generated once and never overwritten.\n",
						entityName,
						entityName,
						entityName)
				.build();

		JavaFile javaFile = JavaFile.builder(pkg, serviceInterface)
				.skipJavaLangImports(true)
				.build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(serviceName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_USER_SERVICE, javaFile.toString()));
	}
}
