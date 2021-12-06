package com.github.filipmalczak.storyteller.impl.jgit.storage.data;

import com.github.filipmalczak.storyteller.api.storage.DocumentsApi;
import com.github.filipmalczak.storyteller.api.storage.FilesApi;
import com.github.filipmalczak.storyteller.api.storage.PropertiesApi;
import com.github.filipmalczak.storyteller.api.storage.Storage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.File;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public final class DirectoryStorage implements Storage {
    @NonNull File root;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    final FilesApi files = new Files(new ManagedDir(new File(root, "files")));

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    final DocumentsApi documents = new Documents(new ManagedDir(new File(root, "documents")));

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    final PropertiesApi properties = new Properties(new ManagedDir(new File(root, "properties")));

    @Override
    public FilesApi files() {
        return this.getFiles();
    }

    @Override
    public DocumentsApi documents() {
        return this.getDocuments();
    }

    @Override
    public PropertiesApi properties() {
        return this.getProperties();
    }
}
