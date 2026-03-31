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
 * including table naming, relationships, audit inheritance, and column constraints.
 */
class EntityGeneratorTest {

	@TempDir
	Path outputDir;

	// -------------------------------------------------------------------------
	// Class-level annotations
	// -------------------------------------------------------------------------

	@Test
	void generate_hasEntityAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@Entity");
	}

	@Test
	void generate_hasTableAnnotationWithDerivedName() {
		// user.yml: tablePrefix=eb_, entity=User → eb_users
		String source = generateSource("user.yml");

		assertThat(source).contains("@Table");
		assertThat(source).contains("eb_users");
	}

	@Test
	void generate_hasLombokAnnotations() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@Data");
		assertThat(source).contains("@NoArgsConstructor");
	}

	// -------------------------------------------------------------------------
	// Inheritance
	// -------------------------------------------------------------------------

	@Test
	void generate_extendsBaseEntity_whenAuditEnabled() {
		// user.yml has audit.enabled: true
		String source = generateSource("user.yml");

		assertThat(source).contains("extends BaseEntity");
	}

	@Test
	void generate_doesNotExtendBaseEntity_whenAuditDisabled() {
		// post.yml has audit.enabled: false
		String source = generateSource("post.yml");

		assertThat(source).doesNotContain("extends BaseEntity");
	}

	@Test
	void generate_hasStandaloneIdField_whenAuditDisabled() {
		// When not extending BaseEntity, @Id must be declared directly in the entity
		String source = generateSource("post.yml");

		assertThat(source).contains("@Id");
	}

	// -------------------------------------------------------------------------
	// Field-level annotations
	// -------------------------------------------------------------------------

	@Test
	void generate_fieldHasColumnAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@Column");
	}

	@Test
	void generate_requiredField_hasNullableFalse() {
		// email is required: true in user.yml
		String source = generateSource("user.yml");

		assertThat(source).contains("nullable = false");
	}

	@Test
	void generate_fieldWithMaxLength_hasLengthConstraint() {
		// email maxLength: 255 in user.yml
		String source = generateSource("user.yml");

		assertThat(source).contains("length = 255");
	}

	@Test
	void generate_uniqueField_hasUniqueConstraint() {
		// email unique: true in user.yml
		String source = generateSource("user.yml");

		assertThat(source).contains("unique = true");
	}

	// -------------------------------------------------------------------------
	// MANY_TO_ONE relationship
	// -------------------------------------------------------------------------

	@Test
	void generate_hasManyToOneAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@ManyToOne");
	}

	@Test
	void generate_manyToOne_hasJoinColumnWithFkColumnName() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@JoinColumn");
		assertThat(source).contains("tenant_id");
	}

	@Test
	void generate_manyToOne_hasForeignKeyConstraintName() {
		String source = generateSource("user.yml");

		assertThat(source).contains("fk_eb_users_tenant_id");
	}

	@Test
	void generate_manyToOne_fieldTypeIsRelatedEntity() {
		String source = generateSource("user.yml");

		assertThat(source).contains("TenantEntity");
	}

	// -------------------------------------------------------------------------
	// ONE_TO_MANY relationship
	// -------------------------------------------------------------------------

	@Test
	void generate_hasOneToManyAnnotation() {
		String source = generateSource("user.yml");

		assertThat(source).contains("@OneToMany");
	}

	@Test
	void generate_oneToMany_hasMappedByAttribute() {
		String source = generateSource("user.yml");

		assertThat(source).contains("mappedBy");
		assertThat(source).contains("user");
	}

	@Test
	void generate_oneToMany_hasAddHelperMethod() {
		String source = generateSource("user.yml");

		assertThat(source).contains("addPost(");
	}

	@Test
	void generate_oneToMany_hasRemoveHelperMethod() {
		String source = generateSource("user.yml");

		assertThat(source).contains("removePost(");
	}

	@Test
	void generate_oneToMany_fieldIsInitializedAsList() {
		String source = generateSource("user.yml");

		// Collection should be initialized to avoid NPE
		assertThat(source).contains("new ArrayList");
	}

	// -------------------------------------------------------------------------
	// Output path
	// -------------------------------------------------------------------------

	@Test
	void generate_outputPathIsInPersistencePackage() {
		List<GeneratedArtifact> artifacts = generate("user.yml");

		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).outputPath().toString()).endsWith("persistence/UserEntity.java");
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
		return new EntityGenerator().generate(config);
	}

	private ServiceConfig loadConfig(String yamlResource) {
		URL url = getClass().getResource("/service/" + yamlResource);
		assertThat(url).as("Fixture not found: " + yamlResource).isNotNull();
		return ServiceConfigLoader.load(Paths.get(url.getPath()), outputDir);
	}
}
