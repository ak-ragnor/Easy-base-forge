package com.example.bookstore.book.infrastructure.hook;

import com.example.bookstore.book.infrastructure.hook.base.BookHookBase;
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
}
