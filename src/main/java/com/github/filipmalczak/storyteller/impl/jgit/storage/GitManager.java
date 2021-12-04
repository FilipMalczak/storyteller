package com.github.filipmalczak.storyteller.impl.jgit.storage;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.jgit.api.Git;

import java.io.File;

@RequiredArgsConstructor
@Getter
public class GitManager implements DiskSpaceManager {
    @NonNull File goldenSource;
    @NonNull File tmp;

    @Override
    public Workspace getWorkspace(String name, boolean emptyIfDirty) {
        File workspace = new File(tmp, name);
        if (!workspace.mkdirs() && emptyIfDirty)
            deleteFolder(workspace);
        workspace.mkdirs();
        return new Workspace(name, workspace);
    }

    //https://stackoverflow.com/a/7768086
    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    @Override
    @SneakyThrows
    public WorkingCopy cloneInto(Workspace workspace) {
        Git git = Git.cloneRepository()
            .setDirectory(workspace.getWorkingDir())
            .setURI(goldenSource.toURI().toString())
            .setBare(false)
//            .setNoCheckout(true)
            .call();
        return new WorkingCopy(git, workspace);
    }

    @Override
    @SneakyThrows
    public WorkingCopy open(Workspace workspace) {
        Git git = Git.open(workspace.getWorkingDir());
        return new WorkingCopy(git, workspace);
    }
}
