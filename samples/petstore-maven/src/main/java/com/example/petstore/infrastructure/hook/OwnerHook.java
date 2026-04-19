package com.example.petstore.infrastructure.hook;

import com.example.petstore.domain.model.Owner;
import com.example.petstore.infrastructure.hook.base.OwnerHookBase;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Lifecycle hook for {@code Owner}.
 *
 * <p>Populates audit fields and logs lifecycle events.
 * In a real application, the auditor UUID would come from the security context.
 */
@Component
public class OwnerHook implements OwnerHookBase {

    private static final Logger log = LoggerFactory.getLogger(OwnerHook.class);

    /** Placeholder auditor — replace with SecurityContextHolder lookup in production. */
    private static final UUID SYSTEM_AUDITOR = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    public void beforeSave(Owner owner) {
        if (owner.getCreatedBy() == null) {
            owner.setCreatedBy(SYSTEM_AUDITOR);
        }
        owner.setUpdatedBy(SYSTEM_AUDITOR);
    }

    @Override
    public void afterSave(Owner owner) {
        log.debug("Owner saved: id={}, email={}", owner.getId(), owner.getEmail());
    }

    @Override
    public void beforeDelete(UUID id) {
        log.info("Owner being deleted: id={}", id);
    }
}
