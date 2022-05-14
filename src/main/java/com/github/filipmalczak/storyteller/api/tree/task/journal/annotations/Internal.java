package com.github.filipmalczak.storyteller.api.tree.task.journal.annotations;

import java.lang.annotation.*;

/**
 * Types annotated with this are internal abstract types, used to simplify entry implementation.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Internal {
}
