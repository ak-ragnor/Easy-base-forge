package com.easybase.forge.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
		name = "easybase",
		description = "EasyBase REST Builder — OpenAPI-first Spring Boot REST layer generator.",
		mixinStandardHelpOptions = true,
		version = "0.1.0",
		subcommands = {GenerateCommand.class, CommandLine.HelpCommand.class})
public class EasyBaseForge implements Runnable {

	public static void main(String[] args) {
		int exitCode = new CommandLine(new EasyBaseForge())
				.setCaseInsensitiveEnumValuesAllowed(true)
				.execute(args);
		System.exit(exitCode);
	}

	@Override
	public void run() {
		// No subcommand → print help
		CommandLine.usage(this, System.out);
	}
}
