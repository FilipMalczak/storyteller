package com.github.filipmalczak.storyteller.impl.testimpl;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum TrivialTaskType implements TaskType {
    ROOT(TaskKind.ROOT),
    SEQ_NODE(TaskKind.SEQUENTIAL_NODE),
    PAR_NODE(TaskKind.PARALLEL_NODE),
    LEAF(TaskKind.LEAF);

    TaskKind modifier;
}
