package com.example.bookstore.author.infrastructure.hook;

import com.example.bookstore.author.domain.model.Author;
import com.example.bookstore.author.infrastructure.hook.base.AuthorHookBase;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Developer-owned hook for {@code Author}.
 *
 * <p>Override methods from {@link AuthorHookBase} to add lifecycle behaviour.
 * Example: populate {@code createdBy} from the security context in {@code beforeSave}.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class AuthorHook implements AuthorHookBase {

    private static final UUID SYSTEM_AUDITOR = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    public void beforeSave(Author author) {
        if (author.getCreatedBy() == null) {
            author.setCreatedBy(SYSTEM_AUDITOR);
        }
        author.setUpdatedBy(SYSTEM_AUDITOR);
        if (author.getTenantId() == null) {
            author.setTenantId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        }
    }

    @Override
    public void afterSave(Author author) {
    }

    @Override
    public void beforeDelete(UUID id) {
    }

    @Override
    public void afterDelete(UUID id) {
    }
}
