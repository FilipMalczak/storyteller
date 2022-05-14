package com.github.filipmalczak.storyteller.api.tree.task.journal.annotations;

import java.lang.annotation.*;

/**
 * Use this on sealed interface with static permitted subtype interfaces. It should repesenet "a dimension" of metadata,
 * meaning that subtypes of other types annotated with @MetaDimension should be composable with this. For example, you
 * could have sealed interface Size permits Size.Big, Size.Small and interface Colour permits Colour.Red, Colour.Blue,
 * and put @MetaDimension on Colour and on Size.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaDimension {
}
