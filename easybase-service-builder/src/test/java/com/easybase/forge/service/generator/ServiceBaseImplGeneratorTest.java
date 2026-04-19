package com.easybase.forge.service.generator;

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

class ServiceBaseImplGeneratorTest {

	@TempDir
	Path outputDir;

	@Test
	void generate_isAbstractClass() {
		String source = generateSource("user.yml");

		assertThat(source).contains("abstract class UserServiceBaseImpl");
	}

	@Test
	void generate_implementsServiceBaseInterface() {
		String source = generateSource("user.yml");

		assertThat(source).contains("implements UserServiceBase");
	}

	@Test
	void generate_hasProtectedFinalLocalServiceField() {
		String source = generateSource("user.yml");

		assertThat(source).contains("protected final UserLocalService userLocalService");
	}

	@Test
	void generate_createMethodIsTransactional() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@Transactional");
		assertThat(source).contains("create(");
	}

	@Test
	void generate_updateMethodIsTransactional() {
		String source = generateSource("user.yml");

		assertThat(source).contains("update(");
		assertThat(source).contains("@Transactional");
	}

	@Test
	void generate_findByIdMethodIsReadOnlyTransactional() {
		String source = generateSource("user.yml");

		assertThat(source).contains("readOnly = true");
		assertThat(source).contains("findById(");
	}

	@Test
	void generate_findAllMethodIsReadOnlyTransactional() {
		String source = generateSource("user.yml");

		assertThat(source).contains("readOnly = true");
		assertThat(source).contains("findAll(");
	}

	@Test
	void generate_includesAllCrudMethods_whenAllEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("create(");
		assertThat(source).contains("update(");
		assertThat(source).contains("delete(");
		assertThat(source).contains("findById(");
		assertThat(source).contains("findAll(");
	}

	@Test
	void generate_omitsDeleteMethod_whenCrudDeleteDisabled() {
		String source = generateSource("product.yml");

		assertThat(source).doesNotContain("delete(");
	}

	@Test
	void generate_includesRemainingMethods_whenDeleteDisabled() {
		String source = generateSource("product.yml");

		assertThat(source).contains("create(");
		assertThat(source).contains("update(");
		assertThat(source).contains("findById(");
		assertThat(source).contains("findAll(");
	}

	@Test
	void generate_hasNoHookFieldOrHookCalls() {
		String source = generateSource("user.yml");

		assertThat(source).doesNotContain("beforeCreate");
		assertThat(source).doesNotContain("afterCreate");
		assertThat(source).doesNotContain("UserHook");
	}

	@Test
	void generate_outputPathIsInServiceBasePackage() {
		List<GeneratedArtifact> artifacts = generate("user.yml");

		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).outputPath().toString()).endsWith("user/service/base/UserServiceBaseImpl.java");
	}

	private String generateSource(String yamlResource) {
		List<GeneratedArtifact> artifacts = generate(yamlResource);
		assertThat(artifacts).hasSize(1);
		return artifacts.get(0).content();
	}

	private List<GeneratedArtifact> generate(String yamlResource) {
		ServiceConfig config = loadConfig(yamlResource);
		return new ServiceBaseImplGenerator().generate(config);
	}

	private ServiceConfig loadConfig(String yamlResource) {
		URL url = getClass().getResource("/service/" + yamlResource);
		assertThat(url).as("Fixture not found: " + yamlResource).isNotNull();
		return ServiceConfigLoader.load(Paths.get(url.getPath()), outputDir);
	}
}
