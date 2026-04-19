package com.example.petstore.infrastructure.hook;

import com.example.petstore.domain.model.Pet;
import com.example.petstore.infrastructure.hook.base.PetHookBase;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Lifecycle hook for {@code Pet}.
 *
 * <p>Populates audit fields and enforces default status on new pets.
 */
@Component
public class PetHook implements PetHookBase {

    private static final Logger log = LoggerFactory.getLogger(PetHook.class);

    private static final UUID SYSTEM_AUDITOR = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    public void beforeSave(Pet pet) {
        if (pet.getStatus() == null || pet.getStatus().isBlank()) {
            pet.setStatus("available");
        }
        if (pet.getCreatedBy() == null) {
            pet.setCreatedBy(SYSTEM_AUDITOR);
        }
        pet.setUpdatedBy(SYSTEM_AUDITOR);
    }

    @Override
    public void afterSave(Pet pet) {
        log.debug("Pet saved: id={}, name={}, owner={}", pet.getId(), pet.getName(), pet.getOwnerId());
    }

    @Override
    public void afterUpdate(Pet pet) {
        log.debug("Pet updated: id={}, status={}", pet.getId(), pet.getStatus());
    }

    @Override
    public void beforeDelete(UUID id) {
        log.info("Pet being soft-deleted: id={}", id);
    }
}
