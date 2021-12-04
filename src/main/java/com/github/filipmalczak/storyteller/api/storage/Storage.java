package com.github.filipmalczak.storyteller.api.storage;

public interface Storage {
    FilesApi files();
    DocumentsApi documents();
    PropertiesApi properties();
}
