package com.github.filipmalczak.storyteller.impl.jgit.storage;

import java.io.File;

public interface DiskSpaceManager extends WorkspaceFactory {
    File getGoldenSource();

    File getTmp();

    WorkingCopy cloneInto(Workspace workspace);

    WorkingCopy open(Workspace workspace);
}
