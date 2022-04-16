package com.github.filipmalczak.storyteller.api.storage;

import com.github.filipmalczak.storyteller.api.storage.files.ReadFilesApi;

public interface ReadStorage {
    ReadFilesApi files();
}
