package com.easybase.forge.service.generator.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.easybase.forge.common.io.GeneratedArtifact;
import com.easybase.forge.service.config.ServiceConfig;
import com.easybase.forge.service.config.ServiceConfigLoader;

class PersistenceAdapterBaseGeneratorTest {

	@TempDir
	Path outputDir;

	@Test
	void generate_isAbstractClass() {
		String source = generateSource("user.yml");

		assertThat(source).contains("abstract class UserPersistenceAdapterBase");
	}

	@Test
	void generate_implementsRepository() {
		String source = generateSource("user.yml");

		assertThat(source).contains("implements UserRepository");
	}

	@Test
	void generate_hasConstructorWithJpaRepo() {
		String source = generateSource("user.yml");

		assertThat(source).doesNotContain("@Autowired");
		assertThat(source).contains("UserJpaRepository jpaRepo");
		assertThat(source).contains("protected UserPersistenceAdapterBase(");
	}

	@Test
	void generate_hasHookListField_whenHookEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("List<UserHookBase> hooks");
	}

	@Test
	void generate_hooksInjectedViaConstructor_whenHookEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).doesNotContain("required = false");
		assertThat(source).contains("List<UserHookBase> hooks");
	}

	@Test
	void generate_hasNoHookField_whenHookDisabled() {
		String source = generateSource("post.yml");

		assertThat(source).doesNotContain("hooks");
		assertThat(source).doesNotContain("HookBase");
	}

	@Test
	void generate_saveCallsBeforeAndAfterHooks_whenEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("beforeSave");
		assertThat(source).contains("afterSave");
	}

	@Test
	void generate_deleteCallsBeforeAndAfterHooks_whenEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("beforeDelete");
		assertThat(source).contains("afterDelete");
	}

	@Test
	void generate_hookIterationUsesForEach_whenHookEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("for (UserHookBase h : userHooks)");
	}

	@Test
	void generate_findByIdDelegatesToFindActiveById_whenSoftDeleteEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("findActiveById(");
	}

	@Test
	void generate_findAllDelegatesToFindAllActive_whenSoftDeleteEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("findAllActive()");
	}

	@Test
	void generate_findByIdDelegatesToJpaFindById_whenSoftDeleteDisabled() {
		String source = generateSource("product.yml");

		assertThat(source).doesNotContain("findActiveById");
		assertThat(source).contains("productJpaRepository.findById(");
	}

	@Test
	void generate_deleteByIdSetsSoftDeleteFlag_whenSoftDeleteEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("setDeleted(true)");
		assertThat(source).doesNotContain("jpaRepo.deleteById");
	}

	@Test
	void generate_deleteByIdCallsJpaDeleteById_whenSoftDeleteDisabled() {
		String source = generateSource("user.yml");

		assertThat(source).doesNotContain("jpaRepo.deleteById");
	}

	@Test
	void generate_hasToDomainAbstractMethod() {
		String source = generateSource("user.yml");

		assertThat(source).contains("protected abstract");
		assertThat(source).contains("toDomain(");
	}

	@Test
	void generate_hasToEntityAbstractMethod() {
		String source = generateSource("user.yml");

		assertThat(source).contains("toEntity(");
	}

	@Test
	void generate_outputPathIsInPersistenceBasePackage() {
		List<GeneratedArtifact> artifacts = generate("user.yml");

		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).outputPath().toString())
				.endsWith("user/infrastructure/persistence/base/UserPersistenceAdapterBase.java");
	}

	private String generateSource(String yamlResource) {
		List<GeneratedArtifact> artifacts = generate(yamlResource);
		assertThat(artifacts).hasSize(1);
		return artifacts.get(0).content();
	}

	private List<GeneratedArtifact> generate(String yamlResource) {
		ServiceConfig config = loadConfig(yamlResource);
		return new PersistenceAdapterBaseGenerator().generate(config);
	}

	private ServiceConfig loadConfig(String yamlResource) {
		URL url = getClass().getResource("/service/" + yamlResource);
		assertThat(url).as("Fixture not found: " + yamlResource).isNotNull();
		return ServiceConfigLoader.load(Paths.get(url.getPath()), outputDir);
	}
}
