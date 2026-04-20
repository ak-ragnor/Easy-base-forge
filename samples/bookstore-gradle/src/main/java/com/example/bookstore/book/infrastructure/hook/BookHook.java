package com.example.bookstore.book.infrastructure.hook;

import com.example.bookstore.book.domain.model.Book;
import com.example.bookstore.book.infrastructure.hook.base.BookHookBase;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Developer-owned hook for {@code Book}.
 *
 * <p>Override methods from {@link BookHookBase} to add lifecycle behaviour.
 * Example: populate {@code createdBy} from the security context in {@code beforeSave}.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class BookHook implements BookHookBase {

    private static final UUID SYSTEM_AUDITOR = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    public void beforeSave(Book book) {
        if (book.getCreatedBy() == null) {
            book.setCreatedBy(SYSTEM_AUDITOR);
        }
        book.setUpdatedBy(SYSTEM_AUDITOR);
        if (book.getTenantId() == null) {
            book.setTenantId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        }
    }

    @Override
    public void afterSave(Book book) {
    }

    @Override
    public void beforeDelete(UUID id) {
    }

    @Override
    public void afterDelete(UUID id) {
    }
}
