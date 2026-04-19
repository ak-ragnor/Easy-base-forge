package com.easybase.forge.service.config;

/**
 * Controls how lifecycle hooks are generated and injected.
 *
 * <p>Example YAML:
 * <pre>
 * hook:
 *   enabled: true
 *   multiple: true
 * </pre>
 */
public class HookOptions {

	/**
	 * Whether hook infrastructure is generated at all.
	 * When {@code false}, no hook interface or implementation is generated,
	 * and the base service impl contains no hook invocations.
	 * Default: {@code true}.
	 */
	private boolean enabled = true;

	/**
	 * When {@code true}, the base service impl injects {@code List<EntityHook>}
	 * so multiple hook implementations can be registered as Spring beans.
	 * When {@code false}, a single {@code EntityHook} is injected.
	 * Default: {@code true}.
	 */
	private boolean multiple = true;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}
}
