package com.example.petstore.infrastructure.hook;

import com.example.petstore.infrastructure.hook.base.VaccinationHookBase;
import org.springframework.stereotype.Component;

/**
 * Developer-owned hook for {@code Vaccination}.
 *
 * <p>Override methods from {@link VaccinationHookBase} to add lifecycle behaviour.
 * Example: populate {@code createdBy} from the security context in {@code beforeSave}.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class VaccinationHook implements VaccinationHookBase {
}
