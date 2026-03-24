package com.easybase.forge.core.generator;

import com.easybase.forge.core.config.*;
import com.easybase.forge.core.engine.GeneratorEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies DelegateImpl generation: placement in the .impl sub-package,
 * @Component annotation, and stub method bodies.
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
    void delegateImpl_placedInImplSubPackage() throws Exception {
        engine().generate(specPath());

        Path implFile = outputDir.resolve(
                "com/example/api/pets/delegate/impl/PetsApiDelegateImpl.java");
        assertThat(implFile).exists();
    }

    @Test
    void delegateImpl_notInParentDelegatePackage() throws Exception {
        engine().generate(specPath());

        Path wrongLocation = outputDir.resolve(
                "com/example/api/pets/delegate/PetsApiDelegateImpl.java");
        assertThat(wrongLocation).doesNotExist();
    }

    @Test
    void delegateImpl_hasComponentAnnotationAndImplementsDelegate() throws Exception {
        engine().generate(specPath());

        String content = Files.readString(outputDir.resolve(
                "com/example/api/pets/delegate/impl/PetsApiDelegateImpl.java"));

        assertThat(content).contains("@Component");
        assertThat(content).contains("implements PetsApiDelegate");
    }

    @Test
    void delegateImpl_methodsThrowUnsupported() throws Exception {
        engine().generate(specPath());

        String content = Files.readString(outputDir.resolve(
                "com/example/api/pets/delegate/impl/PetsApiDelegateImpl.java"));

        assertThat(content).contains("Not implemented");
        assertThat(content).contains("UnsupportedOperationException");
    }

    @Test
    void delegateImpl_generatedForEachResource() throws Exception {
        engine().generate(specPath());

        // petstore.yaml has two tags: pets and orders
        assertThat(outputDir.resolve(
                "com/example/api/pets/delegate/impl/PetsApiDelegateImpl.java")).exists();
        assertThat(outputDir.resolve(
                "com/example/api/orders/delegate/impl/OrdersApiDelegateImpl.java")).exists();
    }
}
