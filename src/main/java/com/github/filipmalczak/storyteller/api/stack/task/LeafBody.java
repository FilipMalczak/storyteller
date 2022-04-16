package com.github.filipmalczak.storyteller.api.stack.task;

import com.github.filipmalczak.storyteller.api.storage.ReadWriteStorage;

public non-sealed interface LeafBody extends TaskBody{
    void perform(ReadWriteStorage storage);
}
