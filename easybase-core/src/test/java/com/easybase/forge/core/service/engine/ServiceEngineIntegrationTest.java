package com.easybase.forge.core.service.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.easybase.forge.core.service.config.ServiceConfig;
import com.easybase.forge.core.service.config.ServiceConfigLoader;
import com.easybase.forge.core.writer.GenerationReport;

/**
 * Integration test: runs the full load → plan → generate → write pipeline for
 * the Service Builder and verifies the expected artefacts are produced.
 */
class ServiceEngineIntegrationTest {

	@TempDir
	Path outputDir;

	// -------------------------------------------------------------------------
	// Report validation
	// -------------------------------------------------------------------------

	@Test
	void generate_reportHasNoErrors() {
		GenerationReport report = runEngine("user.yml");

		assertThat(report.hasErrors()).as("Errors: " + report.errorSummary()).isFalse();
	}

	@Test
	void generate_reportCreates12Artifacts_forFullConfig() {
		GenerationReport report = runEngine("user.yml");

		// 12 artifacts:
		//   model/User.java
		//   repository/UserRepository.java
		//   persistence/BaseEntity.java  (audit enabled)
		//   persistence/UserEntity.java
		//   persistence/UserJpaRepository.java
		//   persistence/UserPersistenceAdapter.java
		//   service/base/UserBaseService.java
		//   service/base/UserBaseServiceImpl.java
		//   service/UserService.java
		//   service/UserServiceImpl.java
		//   hook/UserHook.java
		//   hook/UserHookImpl.java
		assertThat(report.created().size()).isEqualTo(12);
	}

	// -------------------------------------------------------------------------
	// Expected files — user entity (full config)
	// -------------------------------------------------------------------------

	@Test
	void generate_createsModelRecord() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/model/User.java");
		assertThat(source).contains("public record User(");
	}

	@Test
	void generate_createsRepositoryInterface() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/repository/UserRepository.java");
		assertThat(source).contains("interface UserRepository");
	}

	@Test
	void generate_createsBaseEntityInPersistencePackage() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/persistence/BaseEntity.java");
		assertThat(source).contains("abstract class BaseEntity");
	}

	@Test
	void generate_createsUserEntity() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/persistence/UserEntity.java");
		assertThat(source).contains("@Entity");
		assertThat(source).contains("extends BaseEntity");
	}

	@Test
	void generate_createsJpaRepository() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/persistence/UserJpaRepository.java");
		assertThat(source).contains("interface UserJpaRepository");
		assertThat(source).contains("findActiveById");
	}

	@Test
	void generate_createsPersistenceAdapter() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/persistence/UserPersistenceAdapter.java");
		assertThat(source).contains("implements UserRepository");
	}

	@Test
	void generate_createsBaseServiceInterface() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/service/base/UserBaseService.java");
		assertThat(source).contains("interface UserBaseService");
	}

	@Test
	void generate_createsBaseServiceImpl() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/service/base/UserBaseServiceImpl.java");
		assertThat(source).contains("abstract class UserBaseServiceImpl");
		assertThat(source).contains("implements UserBaseService");
		assertThat(source).contains("@Transactional");
	}

	@Test
	void generate_createsUserServiceInterface() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/service/UserService.java");
		assertThat(source).contains("interface UserService");
	}

	@Test
	void generate_createsUserServiceImpl() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/service/UserServiceImpl.java");
		assertThat(source).contains("class UserServiceImpl");
	}

	@Test
	void generate_createsHookInterface() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/hook/UserHook.java");
		assertThat(source).contains("interface UserHook");
	}

	@Test
	void generate_createsHookImpl() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/hook/UserHookImpl.java");
		assertThat(source).contains("class UserHookImpl");
	}

	// -------------------------------------------------------------------------
	// Audit disabled — no BaseEntity
	// -------------------------------------------------------------------------

	@Test
	void generate_withAuditDisabled_skipsBaseEntityFile() {
		runEngine("post.yml");

		Path baseEntity = outputDir.resolve("com/example/app/persistence/BaseEntity.java");
		assertThat(baseEntity).doesNotExist();
	}

	@Test
	void generate_withAuditDisabled_stillCreatesOtherPersistenceFiles() throws IOException {
		runEngine("post.yml");

		String entity = readFile("com/example/app/persistence/PostEntity.java");
		assertThat(entity).contains("@Entity");
		assertThat(entity).contains("@Id");
	}

	// -------------------------------------------------------------------------
	// Hook disabled — no hook files
	// -------------------------------------------------------------------------

	@Test
	void generate_withHookDisabled_skipsHookInterface() {
		runEngine("post.yml");

		Path hookFile = outputDir.resolve("com/example/app/hook/PostHook.java");
		assertThat(hookFile).doesNotExist();
	}

	@Test
	void generate_withHookDisabled_skipsHookImpl() {
		runEngine("post.yml");

		Path hookImplFile = outputDir.resolve("com/example/app/hook/PostHookImpl.java");
		assertThat(hookImplFile).doesNotExist();
	}

	// -------------------------------------------------------------------------
	// Never-overwrite semantics
	// -------------------------------------------------------------------------

	@Test
	void generate_secondRun_doesNotOverwriteUserServiceInterface() throws IOException {
		runEngine("user.yml");

		Path userService = outputDir.resolve("com/example/app/service/UserService.java");
		String sentinel = "// SENTINEL — must survive second run\n";
		Files.writeString(userService, sentinel);

		runEngine("user.yml");

		assertThat(Files.readString(userService)).isEqualTo(sentinel);
	}

	@Test
	void generate_secondRun_doesNotOverwriteUserServiceImpl() throws IOException {
		runEngine("user.yml");

		Path userServiceImpl = outputDir.resolve("com/example/app/service/UserServiceImpl.java");
		String sentinel = "// SENTINEL — must survive second run\n";
		Files.writeString(userServiceImpl, sentinel);

		runEngine("user.yml");

		assertThat(Files.readString(userServiceImpl)).isEqualTo(sentinel);
	}

	@Test
	void generate_secondRun_doesNotOverwriteHookImpl() throws IOException {
		runEngine("user.yml");

		Path hookImpl = outputDir.resolve("com/example/app/hook/UserHookImpl.java");
		String sentinel = "// SENTINEL — must survive second run\n";
		Files.writeString(hookImpl, sentinel);

		runEngine("user.yml");

		assertThat(Files.readString(hookImpl)).isEqualTo(sentinel);
	}

	@Test
	void generate_secondRun_doesOverwriteFrameworkFiles() throws IOException {
		runEngine("user.yml");

		Path baseService = outputDir.resolve("com/example/app/service/base/UserBaseService.java");
		FileTime firstModified = Files.getLastModifiedTime(baseService);

		// Brief pause to ensure the file-system timestamp changes
		try {
			Thread.sleep(10);
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}

		runEngine("user.yml");

		FileTime secondModified = Files.getLastModifiedTime(baseService);
		assertThat(secondModified).isGreaterThan(firstModified);
	}

	// -------------------------------------------------------------------------
	// Spot-check generated content
	// -------------------------------------------------------------------------

	@Test
	void generate_baseServiceImplContainsHookIteration() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/service/base/UserBaseServiceImpl.java");
		assertThat(source).contains("for (UserHook h : hooks)");
	}

	@Test
	void generate_persistenceAdapterHasToDomainAndToEntityMethods() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/persistence/UserPersistenceAdapter.java");
		assertThat(source).contains("toDomain(");
		assertThat(source).contains("toEntity(");
	}

	@Test
	void generate_baseEntityHasSoftDeleteField_whenEnabled() throws IOException {
		runEngine("user.yml");

		// soft-delete column lives in BaseEntity (the @MappedSuperclass), not in UserEntity
		String source = readFile("com/example/app/persistence/BaseEntity.java");
		assertThat(source).contains("Boolean deleted");
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private GenerationReport runEngine(String yamlResource) {
		ServiceConfig config = loadConfig(yamlResource);
		return new ServiceEngine(config).generate();
	}

	private ServiceConfig loadConfig(String yamlResource) {
		URL url = getClass().getResource("/service/" + yamlResource);
		assertThat(url).as("Fixture not found: " + yamlResource).isNotNull();
		return ServiceConfigLoader.load(Paths.get(url.getPath()), outputDir);
	}

	private String readFile(String relativePath) throws IOException {
		Path file = outputDir.resolve(relativePath);
		assertThat(file)
				.as("Expected generated file not found: " + relativePath)
				.exists();
		return Files.readString(file);
	}
}
