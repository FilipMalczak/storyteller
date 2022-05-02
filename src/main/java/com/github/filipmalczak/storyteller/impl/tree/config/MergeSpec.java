package com.github.filipmalczak.storyteller.impl.tree.config;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import lombok.NonNull;

public record MergeSpec<Definition, Type extends Enum<Type> & TaskType>(@NonNull Definition definition, @NonNull Type type) {
}
