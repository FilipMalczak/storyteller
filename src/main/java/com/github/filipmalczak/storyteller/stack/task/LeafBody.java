package com.github.filipmalczak.storyteller.stack.task;

import com.github.filipmalczak.storyteller.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.storage.ReadStorage;
import com.github.filipmalczak.storyteller.storage.ReadWriteStorage;

public non-sealed interface LeafBody extends TaskBody{
    void perform(ReadWriteStorage storage);
}
