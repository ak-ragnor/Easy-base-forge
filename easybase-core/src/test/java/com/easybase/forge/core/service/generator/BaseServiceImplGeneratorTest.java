package com.easybase.forge.core.service.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.easybase.forge.core.generator.GeneratedArtifact;
import com.easybase.forge.core.service.config.ServiceConfig;
import com.easybase.forge.core.service.config.ServiceConfigLoader;

/**
 * Tests for {@link BaseServiceImplGenerator}: verifies the abstract base service
 * implementation including hook wiring, transaction annotations, and CRUD method
 * generation based on configuration.
 */
class BaseServiceImplGeneratorTest {

	@TempDir
	Path outputDir;

	// -------------------------------------------------------------------------
	// Class-level declarations
	// -------------------------------------------------------------------------

	@Test
	void generate_isAbstractClass() {
		String source = generateSource("user.yml");

		assertThat(source).contains("abstract class UserBaseServiceImpl");
	}

	@Test
	void generate_hasRequiredArgsConstructorAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@RequiredArgsConstructor");
	}

	@Test
	void generate_implementsBaseServiceInterface() {
		String source = generateSource("user.yml");

		assertThat(source).contains("implements UserBaseService");
	}

	// -------------------------------------------------------------------------
	// Hook field injection
	// -------------------------------------------------------------------------

	@Test
	void generate_injectsHookList_whenMultiple() {
		// user.yml: hook.multiple = true
		String source = generateSource("user.yml");

		assertThat(source).contains("List<UserHook>").contains("hooks");
	}

	@Test
	void generate_injectsSingleHook_whenNotMultiple() {
		// product.yml: hook.multiple = false → ProductHook hook (no List)
		String source = generateSource("product.yml");

		assertThat(source).contains("ProductHook hook");
		assertThat(source).doesNotContain("List<ProductHook>");
	}

	@Test
	void generate_hasNoHookField_whenHookDisabled() {
		// post.yml: hook.enabled = false → no PostHook reference, no hooks field
		String source = generateSource("post.yml");

		assertThat(source).doesNotContain("PostHook");
		// "hooks" as a field/variable (not "hook" which appears in Javadoc comments)
		assertThat(source).doesNotContain("PostHook hooks");
		assertThat(source).doesNotContain("PostHook hook");
	}

	// -------------------------------------------------------------------------
	// Transactional annotations
	// -------------------------------------------------------------------------

	@Test
	void generate_createMethodIsTransactional() {
		String source = generateSource("user.yml");

		// Source must contain @Transactional before the _create method
		assertThat(source).contains("@Transactional");
		assertThat(source).contains("_create(");
	}

	@Test
	void generate_updateMethodIsTransactional() {
		String source = generateSource("user.yml");

		assertThat(source).contains("_update(");
		// @Transactional appears at least once for write methods
		assertThat(source).contains("@Transactional");
	}

	@Test
	void generate_getMethodIsReadOnlyTransactional() {
		String source = generateSource("user.yml");

		assertThat(source).contains("readOnly = true");
		assertThat(source).contains("_get(");
	}

	@Test
	void generate_listMethodIsReadOnlyTransactional() {
		String source = generateSource("user.yml");

		assertThat(source).contains("readOnly = true");
		assertThat(source).contains("_list(");
	}

	// -------------------------------------------------------------------------
	// CRUD method generation based on CrudOptions
	// -------------------------------------------------------------------------

	@Test
	void generate_includesAllCrudMethods_whenAllEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("_create(");
		assertThat(source).contains("_update(");
		assertThat(source).contains("_delete(");
		assertThat(source).contains("_get(");
		assertThat(source).contains("_list(");
	}

	@Test
	void generate_omitsDeleteMethod_whenCrudDeleteDisabled() {
		// product.yml: crud.delete = false
		String source = generateSource("product.yml");

		assertThat(source).doesNotContain("_delete(");
	}

	@Test
	void generate_includesRemainingMethods_whenDeleteDisabled() {
		String source = generateSource("product.yml");

		assertThat(source).contains("_create(");
		assertThat(source).contains("_update(");
		assertThat(source).contains("_get(");
		assertThat(source).contains("_list(");
	}

	// -------------------------------------------------------------------------
	// Hook invocation patterns
	// -------------------------------------------------------------------------

	@Test
	void generate_hookForEachIteration_whenMultiple() {
		// user.yml: hook.multiple = true → for (UserHook h : hooks)
		String source = generateSource("user.yml");

		assertThat(source).contains("for (UserHook h : hooks)");
	}

	@Test
	void generate_hookDirectCall_whenSingle() {
		// product.yml: hook.multiple = false → hook.beforeCreate(entity)
		String source = generateSource("product.yml");

		assertThat(source).contains("hook.beforeCreate(");
	}

	@Test
	void generate_createMethod_callsBeforeAndAfterHooks() {
		String source = generateSource("user.yml");

		assertThat(source).contains("beforeCreate");
		assertThat(source).contains("afterCreate");
	}

	@Test
	void generate_deleteMethod_callsBeforeAndAfterHooks() {
		String source = generateSource("user.yml");

		assertThat(source).contains("beforeDelete");
		assertThat(source).contains("afterDelete");
	}

	@Test
	void generate_updateMethod_callsBeforeAndAfterHooks() {
		String source = generateSource("user.yml");

		assertThat(source).contains("beforeUpdate");
		assertThat(source).contains("afterUpdate");
	}

	@Test
	void generate_noHookCalls_whenHookDisabled() {
		// post.yml: hook.enabled = false
		String source = generateSource("post.yml");

		assertThat(source).doesNotContain("beforeCreate");
		assertThat(source).doesNotContain("afterCreate");
	}

	// -------------------------------------------------------------------------
	// Output path
	// -------------------------------------------------------------------------

	@Test
	void generate_outputPathIsInBaseServicePackage() {
		List<GeneratedArtifact> artifacts = generate("user.yml");

		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).outputPath().toString()).endsWith("service/base/UserBaseServiceImpl.java");
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private String generateSource(String yamlResource) {
		List<GeneratedArtifact> artifacts = generate(yamlResource);
		assertThat(artifacts).hasSize(1);
		return artifacts.get(0).content();
	}

	private List<GeneratedArtifact> generate(String yamlResource) {
		ServiceConfig config = loadConfig(yamlResource);
		return new BaseServiceImplGenerator().generate(config);
	}

	private ServiceConfig loadConfig(String yamlResource) {
		URL url = getClass().getResource("/service/" + yamlResource);
		assertThat(url).as("Fixture not found: " + yamlResource).isNotNull();
		return ServiceConfigLoader.load(Paths.get(url.getPath()), outputDir);
	}
}
