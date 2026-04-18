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
 * Tests for {@link EntityGenerator}: verifies the @Entity JPA class output
 * including table naming, inline trait fields, FK-only relationships, and column constraints.
 */
class EntityGeneratorTest {

	@TempDir
	Path outputDir;

	@Test
	void generate_hasEntityAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@Entity");
	}

	@Test
	void generate_hasTableAnnotationWithDerivedName() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@Table");
		assertThat(source).contains("eb_users");
	}

	@Test
	void generate_hasLombokAnnotations() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@Getter");
		assertThat(source).contains("@Setter");
		assertThat(source).contains("@NoArgsConstructor");
		assertThat(source).contains("@AllArgsConstructor");
		assertThat(source).doesNotContain("callSuper");
	}

	@Test
	void generate_doesNotExtendAnyBaseClass() {
		String source = generateSource("user.yml");

		assertThat(source).doesNotContain("extends ");
	}

	@Test
	void generate_hasIdField() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@Id");
	}

	@Test
	void generate_hasIdFieldEvenWhenAuditDisabled() {
		String source = generateSource("post.yml");

		assertThat(source).contains("@Id");
	}

	@Test
	void generate_fieldHasColumnAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@Column");
	}

	@Test
	void generate_requiredField_hasNullableFalse() {
		String source = generateSource("user.yml");

		assertThat(source).contains("nullable = false");
	}

	@Test
	void generate_fieldWithMaxLength_hasLengthConstraint() {
		String source = generateSource("user.yml");

		assertThat(source).contains("length = 255");
	}

	@Test
	void generate_uniqueField_hasUniqueConstraint() {
		String source = generateSource("user.yml");

		assertThat(source).contains("unique = true");
	}

	@Test
	void generate_auditFieldsInline_whenAuditEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("private Instant createdAt");
		assertThat(source).contains("private Instant updatedAt");
		assertThat(source).contains("UUID createdBy");
		assertThat(source).contains("UUID updatedBy");
	}

	@Test
	void generate_hasCreationTimestampAnnotation_whenAuditEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@CreationTimestamp");
		assertThat(source).contains("@UpdateTimestamp");
	}

	@Test
	void generate_softDeleteFieldInline_whenSoftDeleteEnabled() {
		String source = generateSource("user.yml");

		assertThat(source).contains("private Boolean deleted");
	}

	@Test
	void generate_hasNoAuditFields_whenAuditDisabled() {
		String source = generateSource("post.yml");

		assertThat(source).doesNotContain("createdAt");
		assertThat(source).doesNotContain("deleted");
	}

	@Test
	void generate_oneToOne_hasPlainFkColumnField() {
		String source = generateSource("user.yml");

		assertThat(source).contains("tenant_id");
		assertThat(source).contains("UUID tenantId");
	}

	@Test
	void generate_oneToOne_hasNoManyToOneAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).doesNotContain("@ManyToOne");
	}

	@Test
	void generate_oneToOne_hasNoJoinColumnAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).doesNotContain("@JoinColumn");
	}

	@Test
	void generate_oneToOne_hasNoOneToManyAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).doesNotContain("@OneToMany");
	}

	@Test
	void generate_outputPathIsInDomainEntityPackage() {
		List<GeneratedArtifact> artifacts = generate("user.yml");

		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).outputPath().toString()).endsWith("user/domain/entity/UserEntity.java");
	}

	private String generateSource(String yamlResource) {
		List<GeneratedArtifact> artifacts = generate(yamlResource);
		assertThat(artifacts).hasSize(1);
		return artifacts.get(0).content();
	}

	private List<GeneratedArtifact> generate(String yamlResource) {
		ServiceConfig config = loadConfig(yamlResource);
		return new EntityGenerator().generate(config);
	}

	private ServiceConfig loadConfig(String yamlResource) {
		URL url = getClass().getResource("/service/" + yamlResource);
		assertThat(url).as("Fixture not found: " + yamlResource).isNotNull();
		return ServiceConfigLoader.load(Paths.get(url.getPath()), outputDir);
	}
}
