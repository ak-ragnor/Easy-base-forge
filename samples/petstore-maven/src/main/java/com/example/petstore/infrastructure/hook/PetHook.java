package com.example.petstore.infrastructure.hook;

import com.example.petstore.infrastructure.hook.base.PetHookBase;
import org.springframework.stereotype.Component;

/**
 * Developer-owned hook for {@code Pet}.
 *
 * <p>Override methods from {@link PetHookBase} to add lifecycle behaviour.
 * Example: populate {@code createdBy} from the security context in {@code beforeSave}.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class PetHook implements PetHookBase {
}
