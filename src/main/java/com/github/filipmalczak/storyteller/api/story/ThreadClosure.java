package com.github.filipmalczak.storyteller.api.story;

import com.github.filipmalczak.storyteller.api.storage.Storage;

public interface ThreadClosure {
    void scene(String name, ActionBody<Storage> body);
}
