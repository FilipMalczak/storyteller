package com.github.filipmalczak.storyteller.api.tree.task;

public interface TaskType {
    enum TaskKind {
        ROOT, PARALLEL_NODE, SEQUENTIAL_NODE, LEAF;
    }

    TaskKind getModifier();

    default boolean isRoot(){
        return getModifier() == TaskKind.ROOT;
    }

    default boolean isSequential(){
        return getModifier() == TaskKind.ROOT || getModifier() == TaskKind.SEQUENTIAL_NODE;
    }

    default boolean isLeaf(){
        return getModifier() == TaskKind.LEAF;
    }

    default boolean isParallel(){
        return getModifier() == TaskKind.PARALLEL_NODE;
    }

    default boolean isWriting(){
        return isLeaf();
    }
}
