package com.easybase.forge.maven;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies the post-generation command runner in {@link GenerateMojo}
 * via a package-private accessor.
 */
class PostGenerateCommandTest {

	@TempDir
	Path workingDir;

	/**
	 * Thin subclass that exposes the otherwise-private
	 * {@code runPostGenerateCommand} for direct testing.
	 */
	static class TestableMojo extends GenerateMojo {

		void runCommand(String command, Path dir) throws MojoExecutionException {
			runPostGenerateCommand(command, dir);
		}
	}

	@Test
	void successCommand_doesNotThrow() throws Exception {
		TestableMojo mojo = new TestableMojo();

		mojo.runCommand("true", workingDir);
	}

	@Test
	void echoCommand_writesOutput() throws Exception {
		Path marker = workingDir.resolve("marker.txt");

		TestableMojo mojo = new TestableMojo();

		mojo.runCommand("touch marker.txt", workingDir);

		assertThat(marker).exists();
	}

	@Test
	void failingCommand_throwsMojoExecutionException() {
		TestableMojo mojo = new TestableMojo();

		assertThatThrownBy(() -> mojo.runCommand("exit 1", workingDir))
				.isInstanceOf(MojoExecutionException.class)
				.hasMessageContaining("exit 1")
				.hasMessageContaining("failed");
	}

	@Test
	void nonExistentCommand_throwsMojoExecutionException() {
		TestableMojo mojo = new TestableMojo();

		assertThatThrownBy(() -> mojo.runCommand("exit 42", workingDir)).isInstanceOf(MojoExecutionException.class);
	}
}
