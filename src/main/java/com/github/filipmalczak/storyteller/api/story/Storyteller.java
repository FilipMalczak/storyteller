package com.github.filipmalczak.storyteller.api.story;

import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.story.body.ActionBody;
import com.github.filipmalczak.storyteller.api.story.body.StructureBody;
import com.github.filipmalczak.storyteller.api.story.closure.ArcClosure;

public interface Storyteller<NoSql> {
    void tell(String storyName, StructureBody<ArcClosure<NoSql>, ReadStorage<NoSql>> arcClosure);
}
