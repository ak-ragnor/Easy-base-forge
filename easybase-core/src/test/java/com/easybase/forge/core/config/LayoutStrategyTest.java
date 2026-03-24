package com.easybase.forge.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LayoutStrategyTest {
	@Test
	void multiModule_substitutesMidSegment() {
		LayoutStrategy s = new MultiModuleLayoutStrategy("com.example");

		assertThat(s.resolvePackage("{basePackage}.{resource}.controller", "pets"))
				.isEqualTo("com.example.pets.controller");
	}

	@Test
	void multiModule_substitutesPascalCase() {
		LayoutStrategy s = new MultiModuleLayoutStrategy("com.example");

		assertThat(s.resolvePackage("{basePackage}.{resource}.controller.base", "orders"))
				.isEqualTo("com.example.orders.controller.base");
	}

	@Test
	void multiModule_lowercasesResourceName() {
		LayoutStrategy s = new MultiModuleLayoutStrategy("com.example");

		assertThat(s.resolvePackage("{basePackage}.{resource}.dto", "Pets")).isEqualTo("com.example.pets.dto");
	}

	@Test
	void multiModule_modeIsMultiModule() {
		assertThat(new MultiModuleLayoutStrategy("com.example").mode()).isEqualTo(LayoutMode.MULTI_MODULE);
	}

	@Test
	void flat_stripsResourceMidSegment() {
		LayoutStrategy s = new FlatLayoutStrategy("com.example");

		assertThat(s.resolvePackage("{basePackage}.{resource}.controller", "pets"))
				.isEqualTo("com.example.controller");
	}

	@Test
	void flat_stripsResourceMidSegment_nestedBase() {
		LayoutStrategy s = new FlatLayoutStrategy("com.example");

		assertThat(s.resolvePackage("{basePackage}.{resource}.controller.base", "orders"))
				.isEqualTo("com.example.controller.base");
	}

	@Test
	void flat_stripsResourceAtEnd() {
		LayoutStrategy s = new FlatLayoutStrategy("com.example");

		assertThat(s.resolvePackage("{basePackage}.{resource}", "pets")).isEqualTo("com.example");
	}

	@Test
	void flat_samePackageForAllResources() {
		LayoutStrategy s = new FlatLayoutStrategy("com.example");
		String pattern = "{basePackage}.{resource}.dto";

		assertThat(s.resolvePackage(pattern, "pets")).isEqualTo(s.resolvePackage(pattern, "orders"));
	}

	@Test
	void flat_modeIsFlat() {
		assertThat(new FlatLayoutStrategy("com.example").mode()).isEqualTo(LayoutMode.FLAT);
	}

	@Test
	void generatorConfig_defaultsToFlat() {
		GeneratorConfig config = new GeneratorConfig();
		config.setBasePackage("com.example");

		assertThat(config.getLayoutStrategy().mode()).isEqualTo(LayoutMode.FLAT);
	}

	@Test
	void generatorConfig_flatLayout_returnsFlatStrategy() {
		GeneratorConfig config = new GeneratorConfig();
		config.setBasePackage("com.example");
		OutputConfig output = new OutputConfig();
		output.setLayout(LayoutMode.FLAT);
		config.setOutput(output);

		assertThat(config.getLayoutStrategy().mode()).isEqualTo(LayoutMode.FLAT);
	}

	@Test
	void generatorConfig_resolvePackage_delegatesToStrategy() {
		GeneratorConfig config = new GeneratorConfig();
		config.setBasePackage("com.example");
		OutputConfig output = new OutputConfig();
		output.setLayout(LayoutMode.FLAT);
		config.setOutput(output);

		assertThat(config.resolvePackage("{basePackage}.{resource}.dto", "pets"))
				.isEqualTo("com.example.dto");
	}
}
