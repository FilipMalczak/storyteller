package com.github.filipmalczak.storyteller.api.storage;

import com.github.filipmalczak.storyteller.api.storage.files.ReadWriteFilesApi;

public interface ReadWriteStorage extends ReadStorage {
    @Override
    ReadWriteFilesApi files();
}
