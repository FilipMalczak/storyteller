package com.github.filipmalczak.storyteller.impl.testimpl;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum TrivialTaskType implements TaskType {
    ROOT(TaskTypeModifier.ROOT),
    NODE(TaskTypeModifier.NONE),
    LEAF(TaskTypeModifier.LEAF);

    TaskTypeModifier modifier;
}
