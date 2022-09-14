package com.github.filipmalczak.storyteller.api.story.closure;

import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.common.ActionBody;
import com.github.filipmalczak.storyteller.api.story.body.StructureBody;

public interface ArcClosure<NoSql> {
    void thread(String thread, StructureBody<ThreadClosure<NoSql>, ReadStorage<NoSql>> body);

    void arc(String arc, StructureBody<ArcClosure<NoSql>, ReadStorage<NoSql>> body);

    <K, E> void decision(String decision, ActionBody<DecisionClosure<K, E, NoSql>> body);
}
