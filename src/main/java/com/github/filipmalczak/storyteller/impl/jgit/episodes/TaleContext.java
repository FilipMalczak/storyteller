package com.github.filipmalczak.storyteller.impl.jgit.episodes;

import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.WorkingCopy;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class TaleContext {
    @NonNull Workspace workspace;
    @NonNull DiskSpaceManager manager;

    @Getter(lazy = true)
    WorkingCopy workingCopy = manager.open(workspace);
}
