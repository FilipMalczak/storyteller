package com.github.filipmalczak.storyteller.api.tree.task;

public interface TaskType {
    enum TaskTypeModifier { //fixme awful name
        NONE, ROOT, PARALLEL, LEAF;

    }

    TaskTypeModifier getModifier();

    default boolean isRoot(){
        return getModifier() == TaskTypeModifier.ROOT;
    }

    default boolean isLeaf(){
        return getModifier() == TaskTypeModifier.LEAF;
    }

    default boolean isParallel(){
        return getModifier() == TaskTypeModifier.PARALLEL;
    }

    default boolean isWriting(){
        return isParallel() || isLeaf();
    }
}
