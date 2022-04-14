package com.github.filipmalczak.storyteller.storage;

public interface ReadWriteStorage extends ReadStorage {
    @Override
    ReadWriteFilesApi files();
}
