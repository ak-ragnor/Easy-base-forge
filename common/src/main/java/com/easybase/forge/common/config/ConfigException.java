package com.easybase.forge.common.config;

/** Thrown when a configuration file is missing, invalid, or fails validation. */
public class ConfigException extends RuntimeException {

	public ConfigException(String message) {
		super(message);
	}

	public ConfigException(String message, Throwable cause) {
		super(message, cause);
	}
}
