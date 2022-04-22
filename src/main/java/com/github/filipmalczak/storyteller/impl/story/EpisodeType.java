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
    STORY(TaskTypeModifier.ROOT),
    ARC(TaskTypeModifier.NONE),
    DECISION(TaskTypeModifier.CHOICE),
    THREAD(TaskTypeModifier.NONE),
    SCENE(TaskTypeModifier.LEAF);

    TaskTypeModifier modifier;
}
