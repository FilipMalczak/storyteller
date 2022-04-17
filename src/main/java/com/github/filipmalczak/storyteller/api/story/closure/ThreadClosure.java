package com.github.filipmalczak.storyteller.api.story.closure;

import com.github.filipmalczak.storyteller.api.storage.ReadWriteStorage;
import com.github.filipmalczak.storyteller.api.story.body.ActionBody;
import com.github.filipmalczak.storyteller.api.story.body.StructureBody;

public interface ThreadClosure<NoSql> {
    void scene(String name, ActionBody<ReadWriteStorage<NoSql>> body);
}
