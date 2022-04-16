package com.github.filipmalczak.storyteller.api.stack.task.body;

import com.github.filipmalczak.storyteller.api.storage.ReadWriteStorage;

@FunctionalInterface
public non-sealed interface LeafBody extends TaskBody{
    void perform(ReadWriteStorage storage);
}
