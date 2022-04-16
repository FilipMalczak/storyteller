package com.github.filipmalczak.storyteller.api.story;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public enum Episodes implements TaskType {
    STORY(true, false),
    ARC(false, false),
    THREAD(false, false),
    SCENE(false, true);

    boolean root;
    boolean leaf;
}
