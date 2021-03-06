package com.github.filipmalczak.storyteller.impl.story;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public enum EpisodeType implements TaskType {
    STORY(TaskKind.ROOT),
    ARC(TaskKind.SEQUENTIAL_NODE),
    DECISION(TaskKind.PARALLEL_NODE),
    THREAD(TaskKind.SEQUENTIAL_NODE),
    SCENE(TaskKind.LEAF);

    TaskKind modifier;
}
