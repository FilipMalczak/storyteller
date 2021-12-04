package com.github.filipmalczak.storyteller.impl.jgit.storage;

public interface WorkspaceFactory {
    Workspace getWorkspace(String name, boolean emptyIfDirty);

    default Workspace getWorkspace(String name){
        return getWorkspace(name, false);
    }

}
