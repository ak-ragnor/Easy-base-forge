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
 * Generates the developer-owned Spring Data JPA repository interface for the configured entity.
 *
 * <p>This file is generated once and <strong>never overwritten</strong> on subsequent runs.
 * Developers add custom JPQL or native query methods here.
 *
 * <p>Example output for entity {@code Pet}:
 * <pre>
 * public interface PetJpaRepository extends PetJpaRepositoryBase {
 *     // Add custom JPQL or native queries here.
 * }
 * </pre>
 */
public class JpaRepositoryGenerator implements ServiceArtifactGenerator {

	@Override
	public List<GeneratedArtifact> generate(ServiceConfig config) {
		String pkg = ServiceGeneratorUtils.persistencePackage(config);
		String jpaRepoName = ServiceGeneratorUtils.jpaRepositoryName(config);
		String jpaRepoBasePkg = ServiceGeneratorUtils.persistenceBasePackage(config);
		String jpaRepoBaseName = ServiceGeneratorUtils.jpaRepositoryBaseName(config);
		String entityName = ServiceGeneratorUtils.entityName(config);

		ClassName jpaRepoBaseType = ClassName.get(jpaRepoBasePkg, jpaRepoBaseName);

		TypeSpec.Builder builder = TypeSpec.interfaceBuilder(jpaRepoName)
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(jpaRepoBaseType)
				.addJavadoc(
						"Developer-owned Spring Data JPA repository for {@code $L}.\n\n"
								+ "<p>Add custom JPQL or native query methods here. "
								+ "This interface is used internally by {@code $LPersistenceAdapterBase}.\n\n"
								+ "<p>This file is generated once and never overwritten.\n",
						entityName,
						entityName);

		ServiceGeneratorUtils.applyAuthors(builder, config);
		TypeSpec jpaRepo = builder.build();

		JavaFile javaFile =
				JavaFile.builder(pkg, jpaRepo).skipJavaLangImports(true).build();

		Path outputDir = ServiceGeneratorUtils.packageToPath(config.getResolvedOutputDirectory(), pkg);
		Path outputPath = outputDir.resolve(jpaRepoName + ".java");

		return List.of(new GeneratedArtifact(outputPath, ArtifactType.SERVICE_JPA_REPOSITORY, javaFile.toString()));
	}
}
