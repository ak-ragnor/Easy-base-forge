package com.easybase.forge.core.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.easybase.forge.core.config.*;
import com.easybase.forge.core.engine.GeneratorEngine;

/**
 * Verifies the DelegateImpl base/impl split:
 * - {Resource}ApiDelegateImplBase in delegate.impl.base (always overwritten, all stubs)
 * - {Resource}ApiDelegateImpl in delegate.impl (create-once, @Component, extends base)
 */
class DelegateImplTest {

	@TempDir
	Path outputDir;

	private GeneratorEngine engine() {
		GeneratorConfig config = new GeneratorConfig();
		config.setBasePackage("com.example.api");

		GenerateOptions opts = new GenerateOptions();
		opts.setDelegateImpl(true);
		config.setGenerate(opts);

		OutputConfig output = new OutputConfig();
		output.setLayout(LayoutMode.MULTI_MODULE);
		config.setOutput(output);

		config.withOutputDirectory(outputDir);

		return new GeneratorEngine(config);
	}

	private Path specPath() throws Exception {
		URL url = getClass().getResource("/specs/petstore.yaml");

		assertThat(url).as("petstore.yaml not found").isNotNull();

		return Paths.get(url.toURI());
	}

	@Test
	void delegateImplBase_placedInImplBaseSubPackage() throws Exception {
		engine().generate(specPath());

		Path baseFile = outputDir.resolve("com/example/api/pets/delegate/impl/base/PetsApiDelegateImplBase.java");

		assertThat(baseFile).exists();
	}

	@Test
	void delegateImplBase_isAbstractAndImplementsDelegate() throws Exception {
		engine().generate(specPath());

		String content = Files.readString(
				outputDir.resolve("com/example/api/pets/delegate/impl/base/PetsApiDelegateImplBase.java"));

		assertThat(content).contains("abstract class PetsApiDelegateImplBase");
		assertThat(content).contains("implements PetsApiDelegate");
	}

	@Test
	void delegateImplBase_hasNoComponentAnnotation() throws Exception {
		engine().generate(specPath());

		String content = Files.readString(
				outputDir.resolve("com/example/api/pets/delegate/impl/base/PetsApiDelegateImplBase.java"));

		assertThat(content).doesNotContain("@Component");
	}

	@Test
	void delegateImplBase_methodsThrowUnsupported() throws Exception {
		engine().generate(specPath());

		String content = Files.readString(
				outputDir.resolve("com/example/api/pets/delegate/impl/base/PetsApiDelegateImplBase.java"));

		assertThat(content).contains("Not implemented");
		assertThat(content).contains("UnsupportedOperationException");
	}

	@Test
	void delegateImpl_placedInImplSubPackage() throws Exception {
		engine().generate(specPath());

		Path implFile = outputDir.resolve("com/example/api/pets/delegate/impl/PetsApiDelegateImpl.java");

		assertThat(implFile).exists();
	}

	@Test
	void delegateImpl_hasComponentAndExtendsBase() throws Exception {
		engine().generate(specPath());

		String content =
				Files.readString(outputDir.resolve("com/example/api/pets/delegate/impl/PetsApiDelegateImpl.java"));

		assertThat(content).contains("@Component");
		assertThat(content).contains("extends PetsApiDelegateImplBase");
	}

	@Test
	void delegateImpl_notInParentDelegatePackage() throws Exception {
		engine().generate(specPath());

		assertThat(outputDir.resolve("com/example/api/pets/delegate/PetsApiDelegateImpl.java"))
				.doesNotExist();
	}

	@Test
	void delegateImpl_generatedForEachResource() throws Exception {
		engine().generate(specPath());

		assertThat(outputDir.resolve("com/example/api/pets/delegate/impl/base/PetsApiDelegateImplBase.java"))
				.exists();
		assertThat(outputDir.resolve("com/example/api/pets/delegate/impl/PetsApiDelegateImpl.java"))
				.exists();
		assertThat(outputDir.resolve("com/example/api/orders/delegate/impl/base/OrdersApiDelegateImplBase.java"))
				.exists();
		assertThat(outputDir.resolve("com/example/api/orders/delegate/impl/OrdersApiDelegateImpl.java"))
				.exists();
	}
}
