package com.easybase.forge.core.service.generator;

import java.nio.file.Path;
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
 * Generates the domain repository interface for the configured entity.
 *
 * <p>The generated interface extends {@link com.easybase.service.runtime.BaseRepository}
 * and acts as the boundary between the service layer and the persistence layer.
 * The service layer depends only on this interface — never on Spring Data JPA directly.
 *
 * <p>Example output for entity {@code User} with {@code idType: UUID}:
 * <pre>
 * public interface UserRepository extends BaseRepository&lt;User, UUID&gt; {}
 * </pre>
 */
public class RepositoryGenerator implements ServiceArtifactGenerator {

	private static final String BASE_REPOSITORY_PACKAGE = "com.easybase.service.runtime";
	private static final String BASE_REPOSITORY_CLASS = "BaseRepository";

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.repositoryPackage(config);
		String repoName = ServiceGeneratorUtils.repositoryName(config);
		String modelPkg = ServiceGeneratorUtils.modelPackage(config);
		String entityName = ServiceGeneratorUtils.entityName(config);

		ClassName entityType = ClassName.get(modelPkg, entityName);
		ClassName baseRepo = ClassName.get(BASE_REPOSITORY_PACKAGE, BASE_REPOSITORY_CLASS);
		ParameterizedTypeName superInterface =
				ParameterizedTypeName.get(baseRepo, entityType, ServiceGeneratorUtils.resolveIdType(config));

		TypeSpec repoInterface = TypeSpec.interfaceBuilder(repoName)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(superInterface)
				.addJavadoc(
						"Repository interface for {@link $T}.\n\n"
								+ "<p>The service layer depends only on this interface. "
								+ "The persistence implementation is provided by "
								+ "{@code $LPersistenceAdapter}.\n",
						entityType,
						entityName)
				.build();

		JavaFile javaFile =
				JavaFile.builder(pkg, repoInterface).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(repoName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_REPOSITORY, javaFile.toString()));
	}
}
