package com.github.filipmalczak.storyteller.api.stack.task;

import java.util.Optional;

public interface TaskType {
    enum TaskTypeModifier { //fixme awful name
        NONE, ROOT, CHOICE, LEAF;

    }

    TaskTypeModifier getModifier();

    default boolean isRoot(){
        return getModifier() == TaskTypeModifier.ROOT;
    }

    default boolean isLeaf(){
        return getModifier() == TaskTypeModifier.LEAF;
    }

    default boolean isChoice(){
        return getModifier() == TaskTypeModifier.CHOICE;
    }

    default boolean isStructuralTask(){
        return getModifier() == TaskTypeModifier.NONE;
    }
}
