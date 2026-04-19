package com.easybase.forge.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.easybase.forge.common.config.layout.FlatLayoutStrategy;
import com.easybase.forge.common.config.layout.LayoutStrategy;
import com.easybase.forge.common.config.layout.MultiModuleLayoutStrategy;

class LayoutStrategyTest {
	@Test
	void multiModule_substitutesMidSegment() {
		LayoutStrategy s = new MultiModuleLayoutStrategy("com.example");

		assertThat(s.resolvePackage("{basePackage}.{module}.controller", "pets"))
				.isEqualTo("com.example.pets.controller");
	}

	@Test
	void multiModule_substitutesModuleInBase() {
		LayoutStrategy s = new MultiModuleLayoutStrategy("com.example");

		assertThat(s.resolvePackage("{basePackage}.{module}.controller.base", "orders"))
				.isEqualTo("com.example.orders.controller.base");
	}

	@Test
	void multiModule_lowercasesModuleName() {
		LayoutStrategy s = new MultiModuleLayoutStrategy("com.example");

		assertThat(s.resolvePackage("{basePackage}.{module}.dto", "Pets")).isEqualTo("com.example.pets.dto");
	}

	@Test
	void multiModule_modeIsMultiModule() {
		assertThat(new MultiModuleLayoutStrategy("com.example").mode()).isEqualTo(LayoutMode.MULTI_MODULE);
	}

	@Test
	void flat_stripsModuleMidSegment() {
		LayoutStrategy s = new FlatLayoutStrategy("com.example");

		assertThat(s.resolvePackage("{basePackage}.{module}.controller", "pets"))
				.isEqualTo("com.example.controller");
	}

	@Test
	void flat_stripsModuleMidSegment_nestedBase() {
		LayoutStrategy s = new FlatLayoutStrategy("com.example");

		assertThat(s.resolvePackage("{basePackage}.{module}.controller.base", "orders"))
				.isEqualTo("com.example.controller.base");
	}

	@Test
	void flat_stripsModuleAtEnd() {
		LayoutStrategy s = new FlatLayoutStrategy("com.example");

		assertThat(s.resolvePackage("{basePackage}.{module}", "pets")).isEqualTo("com.example");
	}

	@Test
	void flat_samePackageForAllModules() {
		LayoutStrategy s = new FlatLayoutStrategy("com.example");
		String pattern = "{basePackage}.{module}.dto";

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

		assertThat(config.resolvePackage("{basePackage}.{module}.dto", "pets")).isEqualTo("com.example.dto");
	}
}
