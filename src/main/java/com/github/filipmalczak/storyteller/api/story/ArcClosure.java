package com.github.filipmalczak.storyteller.api.story;

public interface ArcClosure {
    void thread(String thread, ActionBody<ThreadClosure> body);

    void arc(String arc, ActionBody<ArcClosure> body);

    <K> void decision(String decision, ActionBody<DecisionClosure<K>> body);
}
