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

class ServiceEngineIntegrationTest {

	@TempDir
	Path outputDir;

	@Test
	void generate_reportHasNoErrors() {
		GenerationReport report = runEngine("user.yml");

		assertThat(report.hasErrors()).as("Errors: " + report.errorSummary()).isFalse();
	}

	@Test
	void generate_reportCreates17Artifacts_forFullConfig() {
		GenerationReport report = runEngine("user.yml");

		assertThat(report.created().size()).isEqualTo(17);
	}

	@Test
	void generate_createsDomainModelBase() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/user/domain/model/UserBase.java");
		assertThat(source).contains("public class UserBase");
		assertThat(source).contains("UUID id");
		assertThat(source).contains("String email");
		assertThat(source).contains("Instant createdAt");
		assertThat(source).contains("Boolean deleted");
	}

	@Test
	void generate_createsDomainModel() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/user/domain/model/User.java");
		assertThat(source).contains("public class User");
		assertThat(source).contains("extends UserBase");
	}

	@Test
	void generate_createsUserEntity() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/user/domain/entity/UserEntity.java");
		assertThat(source).contains("@Entity");
		assertThat(source).doesNotContain("extends ");
		assertThat(source).contains("private Boolean deleted");
		assertThat(source).contains("private Instant createdAt");
	}

	@Test
	void generate_createsRepositoryBase() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/user/infrastructure/repository/base/UserRepositoryBase.java");
		assertThat(source).contains("interface UserRepositoryBase");
	}

	@Test
	void generate_createsRepository() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/user/infrastructure/repository/UserRepository.java");
		assertThat(source).contains("interface UserRepository");
		assertThat(source).contains("extends UserRepositoryBase");
	}

	@Test
	void generate_createsJpaRepositoryBase() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/user/infrastructure/persistence/base/UserJpaRepositoryBase.java");
		assertThat(source).contains("interface UserJpaRepositoryBase");
		assertThat(source).contains("@NoRepositoryBean");
		assertThat(source).contains("findActiveById");
	}

	@Test
	void generate_createsJpaRepository() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/user/infrastructure/persistence/UserJpaRepository.java");
		assertThat(source).contains("interface UserJpaRepository");
		assertThat(source).contains("extends UserJpaRepositoryBase");
	}

	@Test
	void generate_createsPersistenceAdapterBase() throws IOException {
		runEngine("user.yml");

		String source =
				readFile("com/example/app/user/infrastructure/persistence/base/UserPersistenceAdapterBase.java");
		assertThat(source).contains("abstract class UserPersistenceAdapterBase");
		assertThat(source).contains("toDomain(");
		assertThat(source).contains("toEntity(");
	}

	@Test
	void generate_createsPersistenceAdapter() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/user/infrastructure/persistence/UserPersistenceAdapter.java");
		assertThat(source).contains("class UserPersistenceAdapter");
		assertThat(source).contains("extends UserPersistenceAdapterBase");
	}

	@Test
	void generate_createsHookBase() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/user/infrastructure/hook/base/UserHookBase.java");
		assertThat(source).contains("interface UserHookBase");
		assertThat(source).contains("beforeSave");
	}

	@Test
	void generate_createsHook() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/user/infrastructure/hook/UserHook.java");
		assertThat(source).contains("class UserHook");
		assertThat(source).contains("implements UserHookBase");
	}

	@Test
	void generate_createsServiceBase() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/user/service/base/UserServiceBase.java");
		assertThat(source).contains("interface UserServiceBase");
	}

	@Test
	void generate_createsServiceBaseImpl() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/user/service/base/UserServiceBaseImpl.java");
		assertThat(source).contains("abstract class UserServiceBaseImpl");
		assertThat(source).contains("implements UserServiceBase");
		assertThat(source).contains("@Transactional");
	}

	@Test
	void generate_createsLocalService() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/user/service/UserLocalService.java");
		assertThat(source).contains("class UserLocalService");
		assertThat(source).contains("@Service");
	}

	@Test
	void generate_createsServiceFacade() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/user/service/UserService.java");
		assertThat(source).contains("class UserService");
		assertThat(source).contains("@Component");
	}

	@Test
	void generate_withAuditDisabled_stillCreatesEntityFile() throws IOException {
		runEngine("post.yml");

		String entity = readFile("com/example/app/post/domain/entity/PostEntity.java");
		assertThat(entity).contains("@Entity");
		assertThat(entity).contains("@Id");
	}

	@Test
	void generate_withHookDisabled_skipsHookBase() {
		runEngine("post.yml");

		Path hookBase = outputDir.resolve("com/example/app/post/infrastructure/hook/base/PostHookBase.java");
		assertThat(hookBase).doesNotExist();
	}

	@Test
	void generate_withHookDisabled_skipsHook() {
		runEngine("post.yml");

		Path hook = outputDir.resolve("com/example/app/post/infrastructure/hook/PostHook.java");
		assertThat(hook).doesNotExist();
	}

	@Test
	void generate_persistenceAdapterBaseContainsHookIteration() throws IOException {
		runEngine("user.yml");

		String source =
				readFile("com/example/app/user/infrastructure/persistence/base/UserPersistenceAdapterBase.java");
		assertThat(source).contains("for (UserHookBase h : userHooks)");
	}

	@Test
	void generate_serviceBaseImplHasNoHookReferences() throws IOException {
		runEngine("user.yml");

		String source = readFile("com/example/app/user/service/base/UserServiceBaseImpl.java");
		assertThat(source).doesNotContain("UserHook");
		assertThat(source).doesNotContain("beforeSave");
		assertThat(source).doesNotContain("afterSave");
	}

	@Test
	void generate_secondRun_doesNotOverwriteLocalService() throws IOException {
		runEngine("user.yml");

		Path localService = outputDir.resolve("com/example/app/user/service/UserLocalService.java");
		String sentinel = "// SENTINEL — must survive second run\n";
		Files.writeString(localService, sentinel);

		runEngine("user.yml");

		assertThat(Files.readString(localService)).isEqualTo(sentinel);
	}

	@Test
	void generate_secondRun_doesNotOverwriteServiceFacade() throws IOException {
		runEngine("user.yml");

		Path serviceFacade = outputDir.resolve("com/example/app/user/service/UserService.java");
		String sentinel = "// SENTINEL — must survive second run\n";
		Files.writeString(serviceFacade, sentinel);

		runEngine("user.yml");

		assertThat(Files.readString(serviceFacade)).isEqualTo(sentinel);
	}

	@Test
	void generate_secondRun_doesNotOverwriteHook() throws IOException {
		runEngine("user.yml");

		Path hook = outputDir.resolve("com/example/app/user/infrastructure/hook/UserHook.java");
		String sentinel = "// SENTINEL — must survive second run\n";
		Files.writeString(hook, sentinel);

		runEngine("user.yml");

		assertThat(Files.readString(hook)).isEqualTo(sentinel);
	}

	@Test
	void generate_secondRun_doesOverwriteFrameworkFiles() throws IOException {
		runEngine("user.yml");

		Path serviceBase = outputDir.resolve("com/example/app/user/service/base/UserServiceBase.java");
		FileTime firstModified = Files.getLastModifiedTime(serviceBase);

		try {
			Thread.sleep(10);
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}

		runEngine("user.yml");

		FileTime secondModified = Files.getLastModifiedTime(serviceBase);
		assertThat(secondModified).isGreaterThan(firstModified);
	}

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
