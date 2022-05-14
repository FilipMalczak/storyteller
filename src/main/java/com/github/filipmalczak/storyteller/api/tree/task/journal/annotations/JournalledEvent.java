package com.github.filipmalczak.storyteller.api.tree.task.journal.annotations;

import java.lang.annotation.*;

/**
 * Classes annotated with this represent actual jorunal entries.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JournalledEvent {
}
