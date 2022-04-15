package com.github.filipmalczak.storyteller.impl;

import com.github.filipmalczak.storyteller.stack.task.TaskType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public enum TrivialTaskType implements TaskType {
    ROOT(true, false),
    NODE(false, false),
    LEAF(false, true);

    boolean root;
    boolean leaf;
}
