package com.example.bookstore.author.infrastructure.hook;

import com.example.bookstore.author.infrastructure.hook.base.AuthorHookBase;
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
}
