package com.github.filipmalczak.storyteller.api.tree.task.journal.annotations;

import java.lang.annotation.*;

/**
 * Interfaces annotated with this are marker interfaces, used to describe general structure of the entry.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DescribesData {
}