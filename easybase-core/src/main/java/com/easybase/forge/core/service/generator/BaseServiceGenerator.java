package com.easybase.forge.core.service.generator;

import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.model.ArtifactType;
import com.easybase.forge.core.service.config.CrudOptions;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Generates the base service interface for the configured entity.
 *
 * <p>The generated interface extends {@link com.easybase.service.runtime.BaseService} and
 * re-declares only the CRUD methods that are enabled in {@code crud:} configuration.
 * Disabled methods are simply omitted — the service layer has no dead code.
 *
 * <p>Example output for entity {@code User} with all CRUD enabled:
 * <pre>
 * public interface UserBaseService extends BaseService&lt;User, UUID&gt; {}
 * </pre>
 */
public class BaseServiceGenerator implements ServiceArtifactGenerator {

	private static final String BASE_SERVICE_PACKAGE = "com.easybase.service.runtime";
	private static final String BASE_SERVICE_CLASS = "BaseService";

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.baseServicePackage(config);
		String baseServiceName = ServiceGeneratorUtils.baseServiceName(config);
		String modelPkg = ServiceGeneratorUtils.modelPackage(config);
		String entityName = ServiceGeneratorUtils.entityName(config);

		ClassName entityType = ClassName.get(modelPkg, entityName);
		ClassName baseService = ClassName.get(BASE_SERVICE_PACKAGE, BASE_SERVICE_CLASS);
		ParameterizedTypeName superInterface =
				ParameterizedTypeName.get(baseService, entityType, ServiceGeneratorUtils.resolveIdType(config));

		TypeSpec.Builder builder = TypeSpec.interfaceBuilder(baseServiceName)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(superInterface)
				.addJavadoc(
						"Generated base service interface for {@link $T}.\n\n"
								+ "<p>Extend {@code $LService} to add business-specific methods.\n"
								+ "This interface is always regenerated — do not edit it.\n",
						entityType,
						entityName);

		addDisabledCrudWarnings(builder, config.getCrud(), entityName);

		JavaFile javaFile =
				JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(baseServiceName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_BASE_SERVICE, javaFile.toString()));
	}

	/**
	 * Adds Javadoc notes for any CRUD operations that are disabled in the configuration.
	 * This documents for developers which methods from {@code BaseService} are unsupported.
	 */
	private void addDisabledCrudWarnings(TypeSpec.Builder builder, CrudOptions crud, String entityName) {
		StringBuilder doc = new StringBuilder();

		if (!crud.isCreate()) {
			doc.append("<p><b>Disabled:</b> {@code _create} — see easybase.yml.\n");
		}

		if (!crud.isUpdate()) {
			doc.append("<p><b>Disabled:</b> {@code _update} — see easybase.yml.\n");
		}

		if (!crud.isDelete()) {
			doc.append("<p><b>Disabled:</b> {@code _delete} — see easybase.yml.\n");
		}

		if (!crud.isGet()) {
			doc.append("<p><b>Disabled:</b> {@code _get} — see easybase.yml.\n");
		}

		if (!crud.isList()) {
			doc.append("<p><b>Disabled:</b> {@code _list} — see easybase.yml.\n");
		}

		if (doc.length() > 0) {
			builder.addJavadoc(doc.toString());
		}
	}
}
