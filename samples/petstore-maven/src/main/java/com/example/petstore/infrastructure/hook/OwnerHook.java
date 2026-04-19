package com.example.petstore.infrastructure.hook;

import com.example.petstore.infrastructure.hook.base.OwnerHookBase;
import org.springframework.stereotype.Component;

/**
 * Developer-owned hook for {@code Owner}.
 *
 * <p>Override methods from {@link OwnerHookBase} to add lifecycle behaviour.
 * Example: populate {@code createdBy} from the security context in {@code beforeSave}.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class OwnerHook implements OwnerHookBase {
}
