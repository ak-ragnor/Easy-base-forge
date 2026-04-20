package com.example.bookstore.review.infrastructure.hook;

import com.example.bookstore.review.infrastructure.hook.base.ReviewHookBase;
import org.springframework.stereotype.Component;

/**
 * Developer-owned hook for {@code Review}.
 *
 * <p>Override methods from {@link ReviewHookBase} to add lifecycle behaviour.
 * Example: populate {@code createdBy} from the security context in {@code beforeSave}.
 *
 * <p>This file is generated once and never overwritten.
 */
@Component
public class ReviewHook implements ReviewHookBase {
}
